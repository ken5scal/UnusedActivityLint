package googleio.demo;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.client.api.JavaEvaluator;
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
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNewExpression;

import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.ATTR_NAME;
import static com.android.SdkConstants.TAG_ACTIVITY;


/**
 * Checks for an activity that has never called
 */
public class UnusedActivityDetector extends Detector implements Detector.XmlScanner, Detector.JavaPsiScanner {

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
    private List<Location> startActivityLocation = new ArrayList<Location>();

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
        System.out.println("beforeCheckProject");
        if (context.getPhase() == 1) {
            mDeclarations = new HashSet<String>(NUM_OF_ACTIVITIES);
            mReferences = new HashSet<String>(NUM_OF_ACTIVITIES);
            mActivities = new ArrayList<String>();
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

    @Nullable
    @Override
    public List<String> getApplicableMethodNames() {
        System.out.println("getApplicableMethodNames");
        //        list.add(START_ACTIVITY_FOR_RESULT);
//        list.add(START_ACTIVITY_FROM_CHILD);
//        list.add(START_ACTIVITY_FROM_FRAGMENT);
//        list.add(START_ACTIVITY_IF_NEEDED);
        return Collections.singletonList(START_ACTIVITY);
    }

    @Override
    public void visitMethod(JavaContext context, JavaElementVisitor visitor, PsiMethodCallExpression call, PsiMethod method) {
        JavaEvaluator evaluator = context.getEvaluator();
        System.out.println("visitMethod");
        if (evaluator.methodMatches(method, ACTIVITY, true, "android.content.Intent")) {
            startActivityLocation.add(context.getNameLocation(call));
        }
    }

    @Override
    public List<String> getApplicableConstructorTypes() {
        return Collections.singletonList("android.content.Intent");
    }

    @Override
    public void visitConstructor(JavaContext context, JavaElementVisitor visitor, PsiNewExpression node, PsiMethod constructor) {
        System.out.println("visitConstructor");
//        System.out.println("node to string: " + node.toString());
//        System.out.println("node get text: " + node.getText());
        System.out.println("resolve constructor: " + node.resolveConstructor());
//        System.out.println("constructor param list: " + constructor.getParameterList());
//        constructor.getParameterList().getParameters()
        JavaEvaluator evaluator = context.getEvaluator();
//        constructor.getTypeParameterList().getTypeParameters()
//        System.out.println(constructor.getTypeParameterList().getTypeParameters()[0]);
//        System.out.println(constructor.getTypeParameterList().getTypeParameters()[1]);

        for (int i =0;i < constructor.getParameterList().getParametersCount(); i++ ) {
                    System.out.println(constructor.getTypeParameters()[i]);
        }
    }

    @Override
    public void checkClass(JavaContext context, PsiClass node) {
        System.out.println("checkClass");
        if (node == null) {
            return;
        }

        boolean found = false;
        for (PsiMethod constructor : node.getConstructors()) {
//            if (isIntentConstructor(constructor)) {
//                found = true;
//                break;
//            }
        }
    }

//    private static boolean isIntentConstructor(PsiMethod method) {
//        // Accept
//        //  android.content.Intent
//        return false;
//    }

    @Override
    public void beforeCheckFile(@NonNull Context context) {
//        File file = context.file;
//        boolean isJavaFile = LintUtils.isXmlFile()
    }

    @Override
    public void afterCheckFile(Context context) {
        if (context.getProject() == context.getMainProject()) {
            mManifestLocation = Location.create(context.file);
        }
    }

    @Override
    public EnumSet<Scope> getApplicableFiles() {
        return Scope.JAVA_FILE_SCOPE;
    }

    // ---- Implements Detector.XmlScanner ----

    @Override
    public Collection<String> getApplicableElements() {
        System.out.println("getApplicableElements");
        return Collections.singleton(TAG_ACTIVITY);
    }

    @Override
    public void visitElement(XmlContext context, Element activityElement) {
        System.out.println("visitElement");
        if (activityElement.getTagName().equals(TAG_ACTIVITY)) {
            String activityName = activityElement.getAttributeNS(ANDROID_URI, ATTR_NAME);
            mActivities.add(activityName);
        }

//        if (!activityElement.hasAttributeNS(
//                "http://schemas.android.com/apk/res/com.google.io.demo",
//                "exampleString")) {
//            context.report(ISSUE, activityElement, context.getLocation(activityElement),
//                    "Missing required attribute 'exampleString'");
//        }
    }
}
