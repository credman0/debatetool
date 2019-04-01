package gui.speechtools;

import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DOCXExporter {
    public static void export(String html) throws IOException {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph tmpParagraph = document.createParagraph();
        parseSpeech(html, tmpParagraph);
        document.write(new FileOutputStream(new File("out.docx")));
        document.close();
    }

    final static String TAG_REGEX = "<(\\/)?([^>]+)>";
    final static Pattern TAG_PATTERN = Pattern.compile(TAG_REGEX);
    // The html/xml parsing libraries have failed me, so here I am doing it by hand...
    private static void parseSpeech(String html, XWPFParagraph paragraph){
        Matcher matcher = TAG_PATTERN.matcher(html);
        int highlight = 0;
        int underline = 0;
        int big = 0;

        int prevEnd = 0;
        while (prevEnd < html.length()){
            XWPFRun tmpRun = paragraph.createRun();
            if (highlight>0){
                tmpRun.setTextHighlightColor(STHighlightColor.CYAN.toString());
                tmpRun.setFontSize(13);
            }
            if (underline>0){
                tmpRun.setUnderline(UnderlinePatterns.SINGLE);
                tmpRun.setFontSize(13);
            }
            if (big>0){
                tmpRun.setFontSize(13);
                tmpRun.setBold(true);
            }

            if (matcher.find()){
                tmpRun.setText(html.substring(prevEnd,matcher.start()));
                prevEnd = matcher.end();
            }else{
                tmpRun.setText(html.substring(prevEnd,html.length()-1));
                break;
            }

            if (matcher.group(2).equals("br")){
                tmpRun.addBreak();
            }else if (matcher.group(2).equals("h")){
                if ("/".equals(matcher.group(1))){
                    highlight--;
                }else{
                    highlight++;
                }
            }else if (matcher.group(2).equals("u")){
                if ("/".equals(matcher.group(1))){
                    underline--;
                }else{
                    underline++;
                }

                // TODO break this up into seperate types
            }else if (matcher.group(2).equals("n") || matcher.group(2).equals("c") || matcher.group(2).equals("t")) {
                if ("/".equals(matcher.group(1))) {
                    big--;
                } else {
                    big++;
                }
            }else if (matcher.group(2).equals("p")) {
                tmpRun.addBreak();
            }else{
                throw new IllegalStateException("Parse found unrecognized symbol: " + matcher.group());
            }
        }
        if (highlight!=0 || underline!=0 || big != 0){
            throw new IllegalStateException("Parser found no closing symbol for some term");
        }
    }
}