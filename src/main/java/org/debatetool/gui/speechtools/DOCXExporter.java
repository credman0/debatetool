/*
 *                               This program is free software: you can redistribute it and/or modify
 *                                it under the terms of the GNU General Public License as published by
 *                                the Free Software Foundation, version 3 of the License.
 *
 *                                This program is distributed in the hope that it will be useful,
 *                                but WITHOUT ANY WARRANTY; without even the implied warranty of
 *                                MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *                                GNU General Public License for more details.
 *
 *                                You should have received a copy of the GNU General Public License
 *                                along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *                                Copyright (c) 2019 Colin Redman
 */

package org.debatetool.gui.speechtools;

import org.debatetool.gui.SettingsHandler;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DOCXExporter {
    public static void export(String html, String name) throws IOException {
        export(html, new File("exports/"+name));
    }

    public static void export(String html, File file) throws IOException {
        XWPFDocument document = new XWPFDocument();
        parseSpeech(html, document);
        new File("exports").mkdir();
        document.write(new FileOutputStream(file));
        document.close();
    }

    final static String TAG_REGEX = "<(\\/)?([^>]+)>|&#(\\d+);";
    final static Pattern TAG_PATTERN = Pattern.compile(TAG_REGEX);
    /**
     * The html/xml parsing libraries have failed me, so here I am doing it by hand...
     */
    private static void parseSpeech(String html, XWPFDocument document){
        XWPFParagraph paragraph = document.createParagraph();
        Matcher matcher = TAG_PATTERN.matcher(html);
        int highlight = 0;
        int underline = 0;
        int big = 0;

        int prevEnd = 0;
        while (prevEnd < html.length()){
            XWPFRun tmpRun = paragraph.createRun();
            tmpRun.setFontFamily("Arial");
            tmpRun.setFontSize(8);
            if (highlight>0){
                tmpRun.setTextHighlightColor(SettingsHandler.getSetting("color"));
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

            String text;
            if (matcher.find()){
                text = html.substring(prevEnd,matcher.start());
                prevEnd = matcher.end();
            }else{
                tmpRun.setText(html.substring(prevEnd,html.length()-1));
                break;
            }

            if (matcher.group(3)!=null){
                text+=((char)Integer.parseInt(matcher.group(3)));
                tmpRun.setText(text);
                continue;
            }

            tmpRun.setText(text);

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
                if (matcher.group(1) == null){
                    // only when on an opening
                    paragraph = document.createParagraph();
                }
            }else{
                throw new IllegalStateException("Parse found unrecognized symbol: " + matcher.group());
            }
        }
        if (highlight!=0 || underline!=0 || big != 0){
            throw new IllegalStateException("Parser found no closing symbol for some term");
        }
    }
}
