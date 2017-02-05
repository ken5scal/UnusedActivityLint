/demo:781 $ cp customlint.jar ~/.android/lint/customlint.jar 
/demo:782 $ lint --show MyId
MyId
----
Summary: My summary of the issue

Priority: 6 / 10
Severity: Warning
Category: Correctness

My longer explanation of the issue


/demo:783 $ lint --check MyId /demo/Demo
/demo/Demo does not exist.
/demo:784 $ lint --check MyId /demo/workspace/Demo

Scanning Demo: ...............
res/layout/sample_my_custom_view.xml:20: Warning: Missing required attribute 'exampleString' [MyId]
    <com.google.io.demo.MyCustomView
    ^
0 errors, 1 warnings
