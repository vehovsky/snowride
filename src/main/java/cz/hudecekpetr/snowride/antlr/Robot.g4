grammar Robot;

@parser::header {
    import cz.hudecekpetr.snowride.tree.*;
    import cz.hudecekpetr.snowride.lexer.*;
}

file returns [RobotFile File]: section* EOF ;
section returns [RobotSection Section]: testCasesSection | keywordsSection | settingsSection | variablesSection | unknownSection;
unknownSection returns [TextOnlyRobotSection Section]: 'nondef';

// Configuration sections
settingsSection returns [KeyValuePairSection Section] : settingsHeader optionalKeyValuePair*;
settingsHeader returns [SectionHeader SectionHeader]: SETTINGS_CELL restOfRow;
variablesSection returns [KeyValuePairSection Section] : variablesHeader optionalKeyValuePair*;
variablesHeader returns [SectionHeader SectionHeader]: VARIABLES_CELL restOfRow;
keyValuePair returns [LogicalLine Line]: ANY_CELL restOfRow;
optionalKeyValuePair returns [LogicalLine Line]: keyValuePair | emptyLine;

// Keywords
keywordsSection returns [KeywordsSection Section] : keywordsHeader emptyLines? testCase*;
keywordsHeader returns [SectionHeader SectionHeader]: KEYWORDS_CELL restOfRow;

// Test cases
testCasesSection returns [TestCasesSection Section]: testCasesHeader emptyLines? testCase*;
testCase returns [Scenario TestCase]: testCaseName testCaseSteps;
testCasesHeader returns [SectionHeader SectionHeader]: TEST_CASES_CELL restOfRow;
testCaseName returns [Cell Cell]: ANY_CELL restOfRow;
//testCaseSettings returns [Lines Lines]: testCaseSetting*;
testCaseSteps returns [Lines Lines]: stepOrEmptyLine*;
//testCaseSetting returns [LogicalLine LogicalLine]: CELLSPACE TEST_CASE_SETTING_CELL restOfRow;
step returns [LogicalLine LogicalLine]: CELLSPACE ANY_CELL restOfRow;
stepOrEmptyLine returns [LogicalLine LogicalLine]: step | emptyLine;
// General
endOfLine: LINE_SEPARATOR | EOF | ' 'LINE_SEPARATOR;
restOfRow returns [LogicalLine Line]: (CELLSPACE (ANY_CELL CELLSPACE?)* (COMMENT | endOfLine)) | endOfLine;
emptyLines returns [String Trivia]: emptyLine+;
emptyLine: ((CELLSPACE | SINGLE_SPACE)+ (COMMENT | endOfLine)) | LINE_SEPARATOR | COMMENT;


fragment CHARACTER: [\u0001-\u0008\u000e-\u001f\u0021-\u007f\u0080-\uffff];//[^ \t\r\n];//[\u0000-\u0250];
fragment TEXT: (CHARACTER ' '?)* CHARACTER;
fragment BEFORE_SECTION_HEADER:'*'[* ]*;
fragment AFTER_SECTION_HEADER:(([* ]*'*')|);
fragment BASIC_CELLSPACE: ('  '[ \t]*)  |   ('\t'[ \t]*) | (' ''\t'[ \t]*);
fragment ELLIPSIS: '...';
fragment LN_FRAGMENT: ('\n'|'\r\n');

COMMENT: '#'.*? LINE_SEPARATOR;
TEST_CASES_CELL: BEFORE_SECTION_HEADER'Test Cases'AFTER_SECTION_HEADER;
KEYWORDS_CELL: BEFORE_SECTION_HEADER'Keywords'AFTER_SECTION_HEADER;
SETTINGS_CELL: BEFORE_SECTION_HEADER'Settings'AFTER_SECTION_HEADER;
VARIABLES_CELL: BEFORE_SECTION_HEADER'Variables'AFTER_SECTION_HEADER;
CELLSPACE:
    BASIC_CELLSPACE |
    ((BASIC_CELLSPACE | SINGLE_SPACE)? LN_FRAGMENT ELLIPSIS)+ BASIC_CELLSPACE |
    ((BASIC_CELLSPACE | SINGLE_SPACE)? LN_FRAGMENT BASIC_CELLSPACE ELLIPSIS)+ BASIC_CELLSPACE;
LINE_SEPARATOR: LN_FRAGMENT;
//TEST_CASE_SETTING_CELL: '[' TEXT ']';
ANY_CELL: TEXT;
SINGLE_SPACE: ' ';