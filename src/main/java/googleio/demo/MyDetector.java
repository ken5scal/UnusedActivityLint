package googleio.demo;

import java.util.Collection;
import java.util.Collections;

import org.w3c.dom.Element;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.ResourceXmlDetector;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.XmlContext;

public class MyDetector extends ResourceXmlDetector {
    public static final Issue ISSUE = Issue.create(
            "MyId",
            "My brief summary of the issue",
            "My longer explanation of the issue",
            Category.CORRECTNESS, 6, Severity.WARNING,
            new Implementation(MyDetector.class, Scope.RESOURCE_FILE_SCOPE));
    
    @Override
    public Collection<String> getApplicableElements() {
        return Collections.singletonList(
                "com.google.io.demo.MyCustomView");
    }

    @Override
    public void visitElement(XmlContext context, Element element) {
        if (!element.hasAttributeNS(
                "http://schemas.android.com/apk/res/com.google.io.demo",
                "exampleString")) {
            context.report(ISSUE, element, context.getLocation(element),
                    "Missing required attribute 'exampleString'");
        }
    }
}
