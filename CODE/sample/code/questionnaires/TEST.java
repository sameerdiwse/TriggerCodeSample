package sample.code.questionnaires;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class TEST {
  public static void main(String[] args) throws IOException {
    Path path = Paths.get("C:/AlexOPLocal/TEMP/input1.txt");
    List<String> lines = Files.readAllLines(path);

    String text = StringUtils.join(lines, "\n");
    System.out.print("Without placeholders:\n" + text);

    List<String> placeholders = new ArrayList<String>();
    placeholders.add(" Alex");
    placeholders.add(
        "<a href=\"\"https://posten1.fyre.ibm.com:10111/app/jspview/react/grc/dashboard/Home\"\">OP_HOME</a>");
    placeholders.add(
        "<a href=\"\"https://posten1.fyre.ibm.com:10111/app/jspview/react/grc/dashboard/Home\"\">OP_HOME</a>");


    for (int i = 0; i < placeholders.size(); i++) {
      text = text.replace("{" + i + "}", placeholders.get(i));
    }

    System.out.print("\n\n\nWITH placeholders:\n" + text);
  }



}
