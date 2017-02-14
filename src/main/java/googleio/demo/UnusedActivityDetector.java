package googleio.demo;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.Speed;
import com.android.tools.lint.detector.api.XmlContext;

import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.ast.AstVisitor;
import lombok.ast.ClassDeclaration;
import lombok.ast.ClassLiteral;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Node;

import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.ATTR_NAME;
import static com.android.SdkConstants.TAG_ACTIVITY;


/**
 * Checks for an activity that has never called
 */
//public class UnusedActivityDetector extends Detector implements Detector.XmlScanner, Detector.JavaPsiScanner {
public class UnusedActivityDetector extends Detector implements Detector.XmlScanner, Detector.JavaScanner {
    private static final Implementation IMPLEMENTATION = new Implementation(
            UnusedActivityDetector.class,
            EnumSet.of(Scope.MANIFEST, Scope.JAVA_FILE)
    );

    static final Issue ISSUE = Issue.create(
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
    private static final String START_ACTIVITY_FROM_CHILD = "startActivityFromChild";
    private static final String START_ACTIVITY_FROM_FRAGMENT = "startActivityFromFragment";
    private static final String START_ACTIVITY_IF_NEEDED = "startActivityIfNeeded";
    private static final String ACTIVITY = "android.app.Activity";
    private Set<String> mDeclarations;
    private Set<String> mReferences;
    private Map<String, Location> mUnused;
    private Location mManifestLocation;
    private List<String> mActivities;
    List<String> mClasses;
    private List<Location> startActivityLocation = new ArrayList<Location>();

    /**
     * Constructs a new {@link UnusedActivityDetector}
     */
    public UnusedActivityDetector() {
    }

    @Override
    public void beforeCheckProject(@NonNull Context context) {
        System.out.println("beforeCheckProject");
        if (context.getPhase() == 1) {
            mDeclarations = new HashSet<String>(NUM_OF_ACTIVITIES);
            mReferences = new HashSet<String>(NUM_OF_ACTIVITIES);
            mActivities = new ArrayList<String>();
            mClasses = new ArrayList<String>();
        }
    }

    @Override
    public void afterCheckProject(Context context) {
        System.out.println("afterCheckProject");
        if (context.getPhase() == 1) {
            mDeclarations.removeAll(mReferences);
            Set<String> unused = mDeclarations;
            mReferences = null;
            mDeclarations = null;
        }

        System.out.print("  activities: ");
        for (String activity : mActivities) {
            System.out.print(activity);
        }
        System.out.println();

        System.out.print("  activities: ");
        for (String claz : mClasses) {
            System.out.print(claz);
        }
        System.out.println();


        if (context.getProject() == context.getMainProject()
                && !context.getMainProject().isLibrary()
                && mManifestLocation != null) {
            String message = "`Activity.startActivity` was found";
            for (Location l : startActivityLocation) {
                System.out.println("xml: " + mActivities.get(startActivityLocation.indexOf(l)));
                context.report(ISSUE, l, message);
            }
        }
    }

    // ---- Implements Detector.XmlScanner ----

    @Override
    public Collection<String> getApplicableElements() {
        System.out.println("getApplicableElements");
        return Collections.singleton(TAG_ACTIVITY);
    }

    @Override
    public boolean appliesTo(@NonNull Context context, @NonNull File file) {
        return true; // TODO: XML -> true // generated -> false
    }

    @Override
    public void visitElement(XmlContext context, Element activityElement) {
        System.out.println("visitElement");
        if (activityElement.getTagName().equals(TAG_ACTIVITY)) {
            String activityName = activityElement.getAttributeNS(ANDROID_URI, ATTR_NAME);
            mActivities.add(activityName);
        }
    }

    // ---- Implements JavaScanner ----
    @Override
    public EnumSet<Scope> getApplicableFiles() {
        return Scope.JAVA_FILE_SCOPE;
    }

    @Override
    public void beforeCheckFile(@NonNull Context context) {
        System.out.println("beforeCheckFile");
        // TODO: I don't want to  go through generated files.
        File file = context.file;
        if (LintUtils.isXmlFile(file) || LintUtils.isBitmapFile(file)) {
            return;
        }
    }

    @Override
    public void afterCheckFile(Context context) {
        System.out.println("afterCheckFile");
        if (context instanceof JavaContext) {
            System.out.println("Heyyyy, JavaContext is working");
            for (String claz : mClasses) {
                System.out.println(claz);
            }
        }

        if (context.getProject() == context.getMainProject()) {
            mManifestLocation = Location.create(context.file);
        }
    }

    @Override
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        System.out.println("getApplicableNodeTypes");
        return Arrays.<Class<? extends Node>>asList(
                ClassLiteral.class
        );
    }

    @Override
    public AstVisitor createJavaVisitor(JavaContext context) {
        System.out.println("createJavaVisitor");
        return new ForwardingAstVisitor() {
            @Override
            public boolean visitClassLiteral(ClassLiteral node) {
                System.out.println("visitClassLiteralllllllllllll");
                if (!mClasses.contains(node.toString())) {
                    mClasses.add(node.toString());
                }
                return super.visitClassLiteral(node);
            }
        };
    }
}
