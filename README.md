# Simple Dependency Injection Library

Support 3 annotations:

- Component: marking beans.
  - It can be used on methods or classes.
  - It has 2 scopes which are singleton and prototype.
- Configuration: marking classes which contain @Component methods.
  - Those classes are treated like singleton component classes.
- Inject: marking fields in which dependencies are injected.
  - You can specify a class which is prioritized for annotated fields by parameter qualified.
  - Cross-dependence of prototype components behaves like singleton.
- Runner: marking method which will be executed by DIContext.
  - It must be used in a @Component classes.
- ComponentScan: annotate on your main class to specify which package you want to scan component.

  - Default componet scanning will start from your main class package. Therefore only components below your main class package would be found.
  - If @ComponentScan is used, only packages listed in parameter packages are scanned.

SimpleDiApplication should be used like this:

```
public class YourMainClass {
    public static void main(String[] args) {
        SimpleDIApplication.run(YourMainClass.class);
    }
}
```
