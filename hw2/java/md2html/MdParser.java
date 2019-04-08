package md2html;

import java.io.IOException;

public class MdParser {
    private final FileManager fileManager;
    private int index = 0;
    private String paragraph;

    MdParser(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public final String parse() throws IOException {
        StringBuilder data = new StringBuilder();
        while (!(paragraph = fileManager.paragraph()).isEmpty()) {
            StringBuilder parsedParagraph = parseParagraph();
            data.append(parsedParagraph);
        }
        return data.toString();
    }

    private final StringBuilder parseParagraph() {
        index = 0;
        StringBuilder parsedParagraph = new StringBuilder();
        int headerLevel = headerLevelParser();
        if (headerLevel == 0) {
            parsedParagraph.append("<p>");
            parsedParagraph.append(parseText(null, false));
            parsedParagraph.append("</p>");
        } else {
            parsedParagraph.append("<h" + headerLevel + ">");
            parsedParagraph.append(parseText(null, false));
            parsedParagraph.append("</h" + headerLevel + ">");
        }
        parsedParagraph.append("\n");
        return parsedParagraph;
    }

    private final int headerLevelParser() {
        while (paragraph.length() > index && paragraph.charAt(index) == '#') {
            index++;
        }
        if (index != 0 && Character.isWhitespace(paragraph.charAt(index))) {
            index++;
            return (index - 1);
        }
        index = 0;
        return 0;
    }

    private void updateParsedText(StringBuilder parsedText, String endLine) {
        int ind1 = index;
        StringBuilder text = parseText(endLine, false);
        Converter converter = new Converter(text.toString());
        if (converter.checkEndLine(text.length() - converter.convertEndLine(endLine, false).length(), converter.convertEndLine(endLine, false).toString())) {
            parsedText.append(converter.convertEndLine(endLine, true));
            parsedText.append(text);
        } else {
            parsedText.append(endLine);
            index = ind1;
        }
    }

    StringBuilder parseUrl() {
        StringBuilder parsed = parseText("]", false);
        if (paragraph.charAt(index - 1) == ']') {
            if (index < paragraph.length() && paragraph.charAt(index) == '(') {
                index++;
                StringBuilder tmp = parseText(")", true);
                if (paragraph.charAt(index - 1) == ')') {
                    StringBuilder ans = new StringBuilder("<a href='");
                    ans.append(tmp);
                    ans.append("'>");
                    ans.append(parsed);
                    ans.append("</a>");
                    return ans;
                } else return parsed.append(tmp);
            }
        }
        return parsed;
    }

    StringBuilder parseImg() {
        StringBuilder parsed = parseText("]", true);
        if (paragraph.charAt(index - 1) == ']') {
            if (index < paragraph.length() && paragraph.charAt(index) == '(') {
                index++;
                StringBuilder tmp = parseText(")", true);
                if (paragraph.charAt(index - 1) == ')') {
                    StringBuilder ans = new StringBuilder("<img alt='");
                    ans.append(parsed);
                    ans.append("'");
                    ans.append(" src='");
                    ans.append(tmp);
                    ans.append("'>");
                    return ans;
                }
            }
        }
        return parsed;
    }

    private final StringBuilder parseText(String endLine, boolean isImg) {
        StringBuilder parsedText = new StringBuilder();
        boolean skipSymbol = false;
        Converter converter = new Converter(paragraph);
        for (; index < paragraph.length(); ++index) {

            if (skipSymbol) {
                skipSymbol = false;
                char a = paragraph.charAt(index);
                if (a == '*' || a == '_' || a == '`') {
                    parsedText.append(a);
                    continue;
                }
                if (a == '\\') {
                    parsedText.append('\\');
                    continue;
                }
                parsedText.append('\\');
                parsedText.append(paragraph.charAt(index));
                continue;
            }
            if (paragraph.charAt(index) == '!' && index + 1 < paragraph.length() && paragraph.charAt(index + 1) == '[') {
                skipSymbol = false;
                index += 2;
                parsedText.append(parseImg());
                index--;
                continue;
            }
            if (paragraph.charAt(index) == '[') {
                index++;
                parsedText.append(parseUrl());
                index--;
                continue;
            }
            if (converter.checkEndLine(index, endLine)) {
                parsedText.append(converter.convertEndLine(endLine, false));
                index += endLine.length();
                return parsedText;
            }
            if (isImg) {
                parsedText.append(paragraph.charAt(index));
                continue;
            }
            if (converter.checkMarkupSymbols(index)) {
                String markupSymbol = converter.getMarkupSymbols(index);
                index += markupSymbol.length();
                updateParsedText(parsedText, markupSymbol);
                index--;
                continue;
            }
            if (paragraph.charAt(index) == '\\') {
                skipSymbol = true;
                if (paragraph.length() == index + 1) {
                    parsedText.append('\\');
                }
                continue;
            }
            if (converter.checkSpecialSymbol(index)) {
                parsedText.append(converter.convertSpecialSymbol(index));
            } else {
                parsedText.append(paragraph.charAt(index));
            }
        }
        return parsedText;
    }

}
