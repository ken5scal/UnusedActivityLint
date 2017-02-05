package googleio.demo;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.ResourceXmlDetector;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.XmlContext;

import org.w3c.dom.Element;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.android.SdkConstants.TAG_ACTIVITY;

/**
 * Checks for an activity that has never called
 *
 */
public class UnusedActivityDetector extends ResourceXmlDetector implements  Detector.JavaScanner {

    private static final Implementation IMPLEMENTATION = new Implementation(
            UnusedActivityDetector.class,
            EnumSet.of(Scope.MANIFEST,  Scope.ALL_JAVA_FILES)
    );

    public static final Issue ISSUE = Issue.create(
            "UnusedActivities",
            "Unused Activities",
            "Unused activieis make application larger, slow down builds,make vulnerable",
            Category.SECURITY,
            3,
            Severity.WARNING,
            IMPLEMENTATION);

    private static final int NUM_OF_ACTIVITIES = 100; //may be increase the number?
    private static final String START_ACTIVITY = "startActivity";
    private static final String START_ACTIVITY_FOR_RESULT = "startActivityForResult";
    private Set<String> mDeclarations;
    private Set<String> mReferences;
    private Map<String, Location> mUnused;

    /**
     * Constructs a new {@link UnusedActivityDetector}
     */
    public UnusedActivityDetector() {
    }

    @Override
    public boolean appliesTo(@NonNull Context context, @NonNull File file) {
        return true;
    }

    @Override
    public void beforeCheckProject(@NonNull Context context) {
        if (context.getPhase() == 1) {
            mDeclarations = new HashSet<String>(NUM_OF_ACTIVITIES);
            mReferences = new HashSet<String>(NUM_OF_ACTIVITIES);
        }
    }

    @Nullable
    @Override
    public List<String> getApplicableMethodNames() {
        List<String> list = Collections.singletonList(START_ACTIVITY);
        list.add(START_ACTIVITY_FOR_RESULT);
        return list;
    }

    @Override
    public void beforeCheckFile(@NonNull Context context) {
        File file = context.file;
//        boolean isJavaFile = LintUtils.isXmlFile()
    }

    @Override
    public EnumSet<Scope> getApplicableFiles() {
        return Scope.JAVA_FILE_SCOPE;
    }

    @Override
    public Collection<String> getApplicableElements() {
        return Collections.singleton(TAG_ACTIVITY);
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
