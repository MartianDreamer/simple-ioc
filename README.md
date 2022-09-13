# Simple Dependency Injection Library
Support 3 annotations:
* Component: mark beans. It can be used on methods or classes. It has 2 scopes which are singleton and prototype.
* Inject: inject a dependency. It is  used to annotated fields. You can specify a class which is prioritized for annotated field by parameter qualified.
* Runner: run a method. It is used to annotated methods in beans or static methods.
