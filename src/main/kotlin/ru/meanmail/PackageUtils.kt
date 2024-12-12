package ru.meanmail

import com.intellij.execution.ExecutionException
import com.intellij.execution.RunCanceledByUserException
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.jetbrains.python.packaging.*
import com.jetbrains.python.packaging.PyPackageVersion
import com.jetbrains.python.packaging.common.PythonPackage
import com.jetbrains.python.packaging.common.PythonPackageSpecification
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.packaging.repository.PyPackageRepository
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.PythonSdkUtil
import ru.meanmail.notification.Notifier
import kotlinx.coroutines.runBlocking


fun getPythonSdk(project: Project, virtualFile: VirtualFile): Sdk? {
    val module = ModuleUtil.findModuleForFile(virtualFile, project) ?: return null
    val moduleSdk = PythonSdkUtil.findPythonSdk(module) ?: return null
    if (moduleSdk.sdkType is PythonSdkType) {
        return moduleSdk
    }
    return null
}

fun getPythonSdk(psiFile: PsiFile): Sdk? {
    val virtualFile = psiFile.virtualFile ?: return null
    return getPythonSdk(psiFile.project, virtualFile)
}

fun getPackageManager(project: Project, sdk: Sdk): PythonPackageManager {
    return PythonPackageManager.forSdk(project, sdk)
}

fun getPackages(project: Project, sdk: Sdk): List<PythonPackage> {
    val packageManager = getPackageManager(project, sdk)
    return ApplicationManager.getApplication()
        .executeOnPooledThread<List<PythonPackage>> {
            try {
                return@executeOnPooledThread runBlocking {
                    packageManager.reloadPackages().getOrThrow()
                }
            } catch (e: Exception) {
                return@executeOnPooledThread emptyList()
            }
        }.get() ?: return emptyList()
}

fun getPackage(project: Project, sdk: Sdk, packageName: String): PythonPackage? {
    val packages = getPackages(project, sdk)
    val canonizedPackageName = canonicalizeName(packageName)

    return packages.firstOrNull { canonicalizeName(it.name) == canonizedPackageName }
}

fun getInstalledPackages(project: Project, sdk: Sdk): List<PythonPackage> {
    val packages = getPackages(project, sdk)

    return packages.filter { !it.isEditableMode }
}

fun getInstalledVersion(project: Project, sdk: Sdk, packageName: String): PyPackageVersion? {
    val pyPackage = getPackage(project, sdk, packageName) ?: return null
    if (pyPackage.isEditableMode) {
        return null
    }
    return PyPackageVersionNormalizer.normalize(pyPackage.version)
}

fun isInstalled(project: Project, sdk: Sdk, packageName: String): Boolean {
    val pyPackage = getPackage(project, sdk, packageName) ?: return false
    return !pyPackage.isEditableMode
}

class PythonPackageSpecificationImpl(
    override val name: String,
    override val repository: PyPackageRepository?,
    override val versionSpecs: String?
) : PythonPackageSpecification {
    override fun buildInstallationString(): List<String> {
        return listOf(name, versionSpecs ?: "")
    }
}

fun installPackage(
    project: Project,
    sdk: Sdk,
    packageName: String,
    version: String,
    onInstalled: (() -> Unit)?
) {
    val installedVersion = getInstalledVersion(project, sdk, packageName)
    if (installedVersion?.presentableText == version) {
        Notifier.notifyInformation(
            project, "$packageName ($version)", "Successfully installed"
        )
        onInstalled?.invoke()
        return
    }

    val text = "$packageName version $version"
    val title = "Installing '$text'"

    val task = object : Task.Backgroundable(project, title) {
        override fun run(indicator: ProgressIndicator) {
            indicator.text = this.title
            indicator.isIndeterminate = true

            try {
                val packageManager = getPackageManager(project, sdk)
                val packageSpec = PythonPackageSpecificationImpl(packageName, null, version)
                val options = listOf<String>() // Add any necessary options here

                runBlocking {
                    packageManager.installPackage(packageSpec, options)
                }

                val pyPackage = getPackage(project, sdk, packageName)

                if (pyPackage == null || pyPackage.version != version) {
                    Notifier.notifyError(
                        project, title, "Failed. Not installed"
                    )
                    return
                }

                Notifier.notifyInformation(
                    project,
                    "${pyPackage.name} (${pyPackage.version})",
                    "Successfully installed"
                )
                onInstalled?.invoke()
            } catch (e: PyExecutionException) {
                Notifier.notifyError(
                    project, e.command, e.toString()
                )
            } catch (e: ExecutionException) {
                Notifier.notifyError(
                    project, "$packageName==$version", e.toString()
                )
            } catch (e: RunCanceledByUserException) {
                // ignore
            }
        }
    }

    ProgressManager.getInstance().run(task)
}

fun uninstallPackage(
    project: Project,
    sdk: Sdk,
    packageName: String,
    onUninstalled: (() -> Unit)?
) {
    val installedPackage = getPackage(project, sdk, packageName)
    if (installedPackage == null || installedPackage.isEditableMode) {
        Notifier.notifyInformation(
            project, packageName, "Successfully uninstalled"
        )
        onUninstalled?.invoke()
        return
    }

    val title = "Uninstalling '$packageName'"

    val task = object : Task.Backgroundable(project, title) {
        override fun run(indicator: ProgressIndicator) {
            indicator.text = this.title
            indicator.isIndeterminate = true

            try {
                val packageManager = getPackageManager(project, sdk)

                runBlocking {
                    packageManager.uninstallPackage(installedPackage).getOrThrow()
                }

                Notifier.notifyInformation(
                    project,
                    packageName,
                    "Successfully uninstalled"
                )
                onUninstalled?.invoke()
            } catch (e: PyExecutionException) {
                Notifier.notifyError(
                    project, e.command, e.toString()
                )
            } catch (e: RunCanceledByUserException) {
                // ignore
            }
        }

        override fun onCancel() {
            onSuccess()
        }
    }

    ProgressManager.getInstance().run(task)
}

operator fun PyPackageVersion?.compareTo(other: PyPackageVersion?): Int {
    if (this == null && other == null) {
        return 0
    }
    if (this == null) {
        return -1
    }
    if (other == null) {
        return 1
    }
    val partsThis = this.release.split('.')
    val partsOther = other.release.split('.')
    val diff = partsThis.size - partsOther.size
    var a = this
    var b = other
    if (diff != 0) {
        val tail = Array(kotlin.math.abs(diff)) { "0" }
        if (diff > 0) {
            b = other.copy(
                release = (partsOther + tail).joinToString(".")
            )
        } else {
            a = this.copy(
                release = (partsThis + tail).joinToString(".")
            )
        }
    }
    return PyPackageVersionComparator.compare(a, b)
}

fun compareVersions(
    actual: PyPackageVersion?,
    operation: String,
    required: PyPackageVersion?
): Boolean {
    if (operation == "===") {
        return actual?.presentableText == required?.presentableText
    }

    actual ?: return false
    required ?: return false

    if (operation == "==") {
        return actual.compareTo(required) == 0
    }

    if (operation == "~=") {
        val parts = required.release.split('.')
        if (parts.size < 2) {
            return compareVersions(actual, "==", required)
        }
        val partsAsInt = parts.subList(0, parts.size - 2).map { it.toInt() }
        val lastGroup = parts[parts.size - 2].toInt() + 1

        val maxRequired = required.copy(
            release = (partsAsInt + listOf(lastGroup)).joinToString(".")
        )
        return compareVersions(actual, ">=", required) &&
                compareVersions(actual, "<", maxRequired)
    }

    if (operation == "!=") {
        return actual.compareTo(required) != 0
    }

    if (operation == ">=") {
        return compareVersions(actual, "==", required) ||
                compareVersions(actual, ">", required)
    }

    if (operation == "<=") {
        return compareVersions(actual, "==", required) ||
                compareVersions(actual, "<", required)
    }

    if (operation == ">") {
        if (actual.pre != null) {
            return false
        }
        return actual > required
    }
    if (operation == "<") {
        if (actual.pre != null) {
            return false
        }
        return actual < required
    }

    return false
}