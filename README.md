# Simple Dependency Injection Library
Support 3 annotations:
* Component: marking beans. It can be used on methods or classes. It has 2 scopes which are singleton and prototype.
* Configuration: marking classes which contain @Component methods. Those classes are treated like singleton component classes.
* Inject: marking fields in which dependencies are injected. You can specify a class which is prioritized for annotated fields by parameter qualified.
* Runner: marking method which will be executed by DIContext. It must be used in a @Component classes.
