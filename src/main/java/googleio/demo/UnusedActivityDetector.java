package googleio.demo;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.XmlContext;

import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.ast.AstVisitor;
import lombok.ast.ClassLiteral;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Node;

import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.ATTR_NAME;
import static com.android.SdkConstants.TAG_ACTIVITY;


/**
 * Checks for an activity that has never called
 */
public class UnusedActivityDetector extends Detector implements Detector.XmlScanner, Detector.JavaScanner {
    private static final Implementation IMPLEMENTATION = new Implementation(
            UnusedActivityDetector.class,
            EnumSet.of(Scope.MANIFEST, Scope.JAVA_FILE)
    );

    static final Issue ISSUE = Issue.create(
            "UnusedActivities",
            "Unused Activities",
            "Unused activities make application larger, slow down builds, make potential vulnerabilities",
            Category.SECURITY,
            3,
            Severity.WARNING,
            IMPLEMENTATION);

    private Location mManifestLocation;
    private final List<String> mClassNames = new ArrayList<String>();
    private final Map<String, Location.Handle> mDeclaredActivities = new HashMap<String, Location.Handle>();

    /**
     * Constructs a new {@link UnusedActivityDetector}
     */
    public UnusedActivityDetector() {
    }

    @Override
    public void afterCheckProject(Context context) {
        if (context.getProject() == context.getMainProject()
                && !context.getMainProject().isLibrary()
                && mManifestLocation != null) {

            for (String activityName : mDeclaredActivities.keySet()) {
                boolean matched = false;
                System.out.println(activityName);

                for (String className : mClassNames) {
                    System.out.println("    " + className);
                    if (!matched && activityName.matches(".*" + className + ".*")) {
                        matched = true;
                    }
                }
                if (!matched) {
                    context.report(ISSUE,
                            mDeclaredActivities.get(activityName).resolve(),
                            String.format("Unused Activity `%s` Detected", activityName));
                }
            }
        }
    }

    // ---- Implements Detector.XmlScanner ----
    @Override
    public Collection<String> getApplicableElements() {
        return Collections.singleton(TAG_ACTIVITY);
    }

    @Override
    public boolean appliesTo(@NonNull Context context, @NonNull File file) {
        return true; // TODO: XML -> true // generated -> false
    }

    @Override
    public void visitElement(XmlContext context, Element activityElement) {
        if (activityElement.getTagName().equals(TAG_ACTIVITY)
                && !context.getMainProject().isLibrary()) {
            String activityName = activityElement.getAttributeNS(ANDROID_URI, ATTR_NAME);
            if (activityName == null || activityName.isEmpty()) {
                return;
            }
            // If the activity class name starts with a '.', it is shorthand for prepending the
            // package name specified in the manifest.
            if (activityName.startsWith(".")) {
                mDeclaredActivities.put(activityName, context.createLocationHandle(activityElement));
            }
        }
    }

    // ---- Implements JavaScanner ----
    @Override
    public EnumSet<Scope> getApplicableFiles() {
        return Scope.JAVA_FILE_SCOPE;
    }

    @Override
    public void afterCheckFile(Context context) {
        if (context.getProject() == context.getMainProject()) {
            mManifestLocation = Location.create(context.file);
        }
    }

    @Override
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        return Arrays.<Class<? extends Node>>asList(
                ClassLiteral.class
        );
    }

    @Override
    public AstVisitor createJavaVisitor(final JavaContext context) {
        return new ForwardingAstVisitor() {
            @Override
            public boolean visitClassLiteral(ClassLiteral node) {
                String className = node.toString(); // foo.class
                if (className.isEmpty()) {
                    return false;
                }
                className = className.split(Pattern.quote("."), 0)[0];

                if (!mClassNames.contains(className)) {
                    mClassNames.add(className);
                }
                return super.visitClassLiteral(node);
            }
        };
    }
}
