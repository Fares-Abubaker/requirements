package ru.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.psi.PsiElementVisitor
import ru.meanmail.isInstalled
import ru.meanmail.psi.NameReq
import ru.meanmail.quickfix.UninstallPackageQuickFix
import ru.meanmail.getPythonSdk

class UninstalledPackageInspection : RequirementsInspection() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        val project = session.file.project
        val sdk = getPythonSdk(session.file)
        return UninstalledPackageInspectionVisitor(holder, isOnTheFly, session, project, sdk)
    }
}

class UninstalledPackageInspectionVisitor(
    holder: ProblemsHolder,
    onTheFly: Boolean,
    session: LocalInspectionToolSession,
    private val project: Project,
    private val pythonSdk: Sdk?
) : BaseInspectionVisitor(holder, onTheFly, session) {
    override fun visitNameReq(element: NameReq) {
        if (!onTheFly) {
            return
        }
        val packageName = element.name.text ?: return
        val sdk = pythonSdk ?: return
        if (!isInstalled(project, sdk, packageName)) {
            return
        }

        val message = "Uninstall '$packageName'"
        holder.registerProblem(
            element,
            message,
            ProblemHighlightType.INFORMATION,
            UninstallPackageQuickFix(element, message)
        )
    }
}