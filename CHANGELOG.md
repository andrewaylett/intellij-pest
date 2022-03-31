## [Unreleased]
- Use GitHub Actions provided by JetBrains for a more understandable build.

## [0.3.3]
- Forked by Andrew Aylett to eu.aylett.pest
- Update much of the build tooling
- Update many of the dependencies

## [0.3.2]
- Make the plugin dynamically loadable
- Update minimum supported platform version to 201

## [0.3.1]
- Upgrade many dependencies, including pest,
    kotlin, kotlinx.html, gradle, grammar-kit, etc.
- Compatible with latest Rust plugin

## [0.3.0]
- Support exporting live-preview files into HTML
	(right-click in live-preview!).
	The inner-most rule information will be preserved,
	syntax error information will be exported as well.

## [0.2.7]
- Catch an exception (#27)
- Bump pest-ide dependencies
- Start using a custom version of asmble

## [0.2.6]
- Regex-based rule highlight (#22)
- Something that hopefully fixes the index out of bound exception (#20, #24)
- Fix comment lexer (#25)

## [0.2.5]
- Rebuild native bridge with rust nightly published yesterday
- Do not ignore case in lexer
- Hide live preview action in non-pest files

## [0.2.4]
- Fix <code>NoClassDefError</code> when Rust plugin is not installed (#18)
- Access native bridge information
- Live Preview is no longer <code>DumbAware</code>

## [0.2.3]
- Correctly bind doc comments to grammar rules
- Correctly syntax-highlight doc comments
- Re-highlight live preview when pest code is changed
- Support RGB-based colors (starting with <code>#</code>) in live-preview
- Handle syntax errors

## [0.2.2]
- Bug fix on introduce rule (#17)
- Bundle a Pest VM in the plugin
- Support "Live Preview" by <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>P</kbd> (Preview!
	Checkout this video instruction showing a quite work-in-progress on-the-fly syntax highlighter:
	<a href="https://www.youtube.com/watch?v=GchD5b_zBZU">YouTube link</a>)
- Support showing error messages from Pest VM

## [0.2.1]
- Fix some compatibility problems, bump minimum supported platform version to 173
- Fix a critical bug in 0.2.0 (so 0.2.0 update is deleted)

## [0.2.0]
- Introduce rule is now very very usable
	It now *detects existing occurrences and provides options for the generated rule type!*
- Change silent rule color under dark themes
- Migrate to Kotlin 1.3.30, Grammar-Kit 2019.1, IntelliJ Gradle Plugin 0.4.7

## [0.1.7]
- Support using ovewritable names

## [0.1.6]
- Fix built-in rules (#14)

## [0.1.5]
- Fix char lexer (#13)
- Fix psi reference range

## [0.1.4]
- Fix rename problem (#9)
- Support find usages (#12)

## [0.1.3]
- Fix <code>PEEK</code> parsing (#7, #8), contributed by
    <a href="https://github.com/MalteSchledjewski">@MalteSchledjewski</a>
- Clear reference cache correctly (#10, #9)
- Old platform version (like CLion 183) compatibility (#6)
- Add tools menu option: browse website

## [0.1.2]
- Add more grammar pins to improve editing experience
- Rule extraction (this is currently poorly implemented)
- Fix exception (#2)

## [0.1.1]
- Add plugin icon
- Fix lexer (#4)

## [0.1.0]
- Improve completion for rules
- Add code folding
- Add structure view
- Support string literal injection
- Support create file action
- Add Quote handler (automatic insert paired quote)
- Spell checker (for comments/rule names, strings are suppressed)
- Automatically highlight pest code in <code>#[grammar_inline = "..."]</code>
- Recursive rule line marker
- Duplicated rule checker
- Live template completion for <code>COMMENT</code> and <code>WHITESPACE</code>
- Support rule inline (this is very fancy!)
- Fix rename unstable issue
- Add completion for built-in rules

## [0.0.1]
- File icon, different for dark and bright themes
- Completion for rules' names
- Keyword highlight built-in rules
- Rename for rules (and validate your rename!)
- Backspace deletes corresponding parenthesis/bracket/brace as well
- Click to go to definition for rules
- GitHub error reporter
