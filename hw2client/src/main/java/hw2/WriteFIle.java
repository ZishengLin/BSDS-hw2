package hw2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class WriteFile {
     ConcurrentHashMap<Long, Integer> outPutData;
     String outFile;

    public WriteFile(ConcurrentHashMap<Long, Integer> outPutData, String outFile) {
        this.outPutData = outPutData;
        this.outFile = outFile;
    }

    /**
     * Write out the content into the output file.
     */
     void write() {
        try {
            File writeName = new File(outFile); // 相对路径，如果没有则要建立一个新的output。txt文件
            boolean write = writeName.createNewFile();
            if (!write) {
                throw new IOException("Unable to create the file");
            }
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new
                    FileOutputStream(writeName), StandardCharsets.UTF_8));

            for (Map.Entry<Long, Integer> entry : outPutData.entrySet()) {
                StringBuffer line = new StringBuffer();
                line.append(String.valueOf(entry.getKey()));
                line.append(',');
                line.append(String.valueOf(entry.getValue()));
                line.append('\n');
                out.write(line.toString());
            }
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}