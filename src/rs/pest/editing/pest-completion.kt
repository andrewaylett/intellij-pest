package rs.pest.editing

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import icons.PestIcons
import rs.pest.BUILTIN_RULES
import rs.pest.psi.PestGrammarBody

class PestBuiltinCompletionContributor : CompletionContributor() {
	private val builtin = BUILTIN_RULES.map {
		LookupElementBuilder
			.create(it)
			.withTypeText("Builtin")
			.withIcon(PestIcons.PEST)
	}

	init {
		val provider = object : CompletionProvider<CompletionParameters>() {
			override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
				builtin.forEach(result::addElement)
			}
		}
		extend(CompletionType.BASIC, PlatformPatterns
			.psiElement()
			.inside(PlatformPatterns
				.psiElement(PestGrammarBody::class.java)),
			provider)
	}
}
