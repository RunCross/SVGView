package top.crosstun.svg.parse;

import android.graphics.RectF;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import top.crosstun.svg.module.SVGPath;

public class SVGParser {
    public static List<SVGPath> parse(InputStream in, RectF rectF) {
        List<SVGPath> list = new ArrayList<>();
        XmlPullParser parser = Xml.newPullParser();
        RectF mRectF = new RectF();
        try {
            parser.setInput(in, "UTF-8");
            int eventType = parser.getEventType();
            String name;
            SVGPath svgPath = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if ("path".equals(name)) {
                            svgPath = new SVGPath();
                            svgPath.id = parser.getAttributeValue(null, "id");
                            svgPath.title = parser.getAttributeValue(null, "title");
                            svgPath.d = parser.getAttributeValue(null, "d");
                            svgPath.clas = parser.getAttributeValue(null, "class");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if ("path".equals(name) && svgPath != null) {
                            svgPath.path =  PathParse.createPathFromPathData(svgPath.d);
                            //path的边界
                            svgPath.path.computeBounds(mRectF, true);
                            colSVGRect(rectF, mRectF);
                            list.add(svgPath);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    private static void colSVGRect(RectF svgRect, RectF rectF) {
        svgRect.left = Math.min(svgRect.left, rectF.left);
        svgRect.top = Math.min(svgRect.top, rectF.top);
        svgRect.right = Math.max(svgRect.right, rectF.right);
        svgRect.bottom = Math.max(svgRect.bottom, rectF.bottom);
    }
}
