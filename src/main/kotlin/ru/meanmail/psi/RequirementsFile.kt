package ru.meanmail.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.psi.FileViewProvider
import ru.meanmail.fileTypes.RequirementsFileType
import ru.meanmail.getPythonInfo
import ru.meanmail.getPythonSdk
import ru.meanmail.lang.RequirementsLanguage

class RequirementsFile(viewProvider: FileViewProvider) :
    PsiFileBase(viewProvider, RequirementsLanguage) {

    override fun getFileType(): FileType {
        return RequirementsFileType
    }

    override fun toString(): String {
        return "Requirements File"
    }

    fun requirements(): List<Requirement> {
        val requirements = mutableListOf<Requirement>()
        for (child in this.children) {
            if (child is Requirement) {
                requirements.add(child)
            }
        }

        return requirements
    }

    val sdk: Sdk?
        get() {
            return getPythonSdk(this)
        }

    fun enabledRequirements(): List<Requirement> {
        val sdk = sdk ?: return emptyList()
        return requirements().filter { it.enabled(getPythonInfo(sdk).map) }
    }

    fun disabledRequirements(): List<Requirement> {
        val sdk = sdk ?: return requirements()
        return requirements().filter { !it.enabled(getPythonInfo(sdk).map) }
    }

}
