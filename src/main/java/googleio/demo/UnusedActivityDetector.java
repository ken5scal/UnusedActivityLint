package googleio.demo;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassObjectAccessExpression;
import com.intellij.psi.PsiElement;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


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

    static Map<PsiClass, Location> activities = new HashMap<PsiClass, Location>();
    static Set<String> classCalls = new HashSet<String>();

    @Override
    public JavaElementVisitor createPsiVisitor(JavaContext context) {
        return new MyJavaVisitor(context);
    }

    @Override
    public List<Class<? extends PsiElement>> getApplicablePsiTypes() {
        return Arrays.asList(PsiClass.class, PsiClassObjectAccessExpression.class);
    }

    static class MyJavaVisitor extends JavaElementVisitor {

        private static final String ACTIVITY = "android.app.Activity";
        private static final String OBJECT = "java.lang.Object";

        private final JavaContext javaContext;

        private MyJavaVisitor(JavaContext context) {
            this.javaContext = context;
        }

        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);
            PsiClass superClass = aClass.getSuperClass();
            while (superClass != null && !OBJECT.equals(superClass.getQualifiedName())) {
                if (ACTIVITY.equals(superClass.getQualifiedName())) {
                    activities.put(aClass, javaContext.getLocation(aClass));
                }
                superClass = superClass.getSuperClass();
            }
        }

        @Override
        public void visitClassObjectAccessExpression(PsiClassObjectAccessExpression expression) {
            super.visitClassObjectAccessExpression(expression);
            classCalls.add(expression.getOperand().getType().getPresentableText());
        }
    }

    @Override
    public void afterCheckProject(Context context) {
        super.afterCheckProject(context);
        for (PsiClass activity : activities.keySet()) {
            if (!classCalls.contains(activity.getQualifiedName())) {
                context.report(ISSUE, activities.get(activity), "This is the one!!!");
            }
        }
    }
}
