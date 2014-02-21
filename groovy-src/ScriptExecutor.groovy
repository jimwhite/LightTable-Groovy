import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.runtime.StackTraceUtils
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

class ScriptExecutor {
  static def execute(String scriptText) {

    def encoding = 'UTF-8'
    def stream = new ByteArrayOutputStream()
    def printStream = new PrintStream(stream, true, encoding)

    def stacktrace = new StringWriter()
    def errWriter = new PrintWriter(stacktrace)

    def aBinding = new Binding([out: printStream])

    def conf = new CompilerConfiguration()
    conf.addCompilationCustomizers(new ASTTransformationCustomizer(new LogScriptTransform()))

    def emcEvents = []
    def listener = { MetaClassRegistryChangeEvent event ->
      emcEvents << event
    } as MetaClassRegistryChangeEventListener

    GroovySystem.metaClassRegistry.addMetaClassRegistryChangeEventListener listener

    def originalOut = System.out
    def originalErr = System.err

    System.setOut(printStream)
    System.setErr(printStream)

    def result = ""
    try {
      result = new GroovyShell(aBinding, conf).evaluate(scriptText)
    } catch (MultipleCompilationErrorsException e) {
      stacktrace.append(e.message - 'startup failed, Script1.groovy: ')
    } catch (Throwable t) {
      StackTraceUtils.deepSanitize(t)
      t.printStackTrace(errWriter)
    } finally {
      System.setOut(originalOut)
      System.setErr(originalErr)

      GroovySystem.metaClassRegistry.removeMetaClassRegistryChangeEventListener listener
      emcEvents.each { MetaClassRegistryChangeEvent event ->
        GroovySystem.metaClassRegistry.removeMetaClass event.classToUpdate
      }
    }

    [
      result: result == null ? "" : result.toString(),
      outputText: stream.toString(encoding),
      stackTrace: stacktrace.toString(),
      bindings: filterBindings(aBinding)
    ]
  }

  static def filterBindings(binding) {
    binding.variables.findAll {it.key != "out"}.collectEntries {key, val ->
      val?.toString().contains("closure") ? [(key) : val.toString()] : [(key) : val]
    }
  }
}
