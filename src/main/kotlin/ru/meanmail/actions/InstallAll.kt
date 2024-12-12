package ru.meanmail.actions

import com.intellij.execution.ExecutionException
import com.intellij.execution.RunCanceledByUserException
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.psi.PsiFile
import kotlinx.coroutines.runBlocking
import ru.meanmail.getPackageManager
import ru.meanmail.getPythonSdk
import ru.meanmail.lang.RequirementsLanguage
import ru.meanmail.notification.Notifier
import ru.meanmail.psi.NameReq
import ru.meanmail.psi.PathReq
import ru.meanmail.psi.RequirementsFile
import ru.meanmail.psi.UrlReq
import ru.meanmail.reparseOpenedFiles
import com.jetbrains.python.packaging.common.PythonPackageSpecification
import ru.meanmail.PythonPackageSpecificationImpl

class InstallAllAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val psiFile = e.getData(LangDataKeys.PSI_FILE)
        e.presentation.isEnabledAndVisible = psiFile?.language == RequirementsLanguage
    }

    override fun actionPerformed(e: AnActionEvent) {
        val psiFile = e.getData(LangDataKeys.PSI_FILE) as? RequirementsFile ?: return

        val requirements = psiFile.enabledRequirements().filter {
            it is NameReq || it is PathReq || it is UrlReq
        }.map {
            return@map it.requirement
        }

        val title = "Installing ${psiFile.name}"
        val task = InstallTask(requirements, title, psiFile)
        ProgressManager.getInstance().run(task)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT // or EDT if this action needs to be run on the UI thread
    }

    class InstallTask(
        val requirements: List<String>,
        title: String,
        private val psiFile: PsiFile
    ) : Task.Backgroundable(psiFile.project, title, false) {
        override fun run(indicator: ProgressIndicator) {
            indicator.isIndeterminate = true
            try {
                val sdk = getPythonSdk(psiFile)
                if (sdk == null) {
                    Notifier.notifyError(project, title, "No Sdk")
                    return
                }
                val packageManager = getPackageManager(project, sdk)

                for (requirement in requirements) {
                    indicator.text = requirement
                    try {
                        val specification = PythonPackageSpecificationImpl(requirement, null, null)
                        runBlocking {
                            packageManager.installPackage(specification, emptyList())
                        }
                        Notifier.notifyInformation(
                            project, requirement, "Successfully installed",
                        )
                    } catch (e: ExecutionException) {
                        Notifier.notifyError(project, requirement, e.toString())
                    }
                }
                reparseOpenedFiles(project)
            } catch (e: RunCanceledByUserException) {
                // ignore
            }
        }
    }
}