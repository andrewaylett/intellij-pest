package rs.pest.rust

import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiWhiteSpace
import org.rust.lang.core.psi.RsLitExpr
import org.rust.lang.core.psi.RsMetaItem
import org.rust.lang.core.psi.RsOuterAttr
import rs.pest.PestLanguage
import rs.pest.psi.childrenWithLeaves

class InlineGrammarInjector : LanguageInjector {
	override fun getLanguagesToInject(host: PsiLanguageInjectionHost, places: InjectedLanguagePlaces) {
		if (host !is RsLitExpr) return
		if (isInlineGrammar(host)) places.addPlace(PestLanguage.INSTANCE, TextRange(1, host.textLength - 1), null, null)
	}

	/// Do we need to check the existence of `#[derive(Parser)]` as well?
	private fun isInlineGrammar(host: RsLitExpr): Boolean {
		// Should be inside of a #[]
		val parent = host.parent as? RsMetaItem ?: return false
		// Should have a #[derive(Parser)] before
		if (parent.parent !is RsOuterAttr) return false
		// Should be grammar_inline = "bla"
		val peers = parent.childrenWithLeaves.filter { it !is PsiWhiteSpace }.toList()
		if (peers.size != 3) return false
		if (peers[0].text != "grammar_inline") return false
		if (peers[1].text != "=") return false
		return peers[2] === host
	}
}
