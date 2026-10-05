import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class GrandParent {

    // Mapper 阶段：为每一行生成两种不同的键值对
    public static class GrandMapper extends Mapper<Object, Text, Text, Text> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString().trim();
            // 跳过表头
            if (line.startsWith("child") || line.isEmpty()) return;

            // 按空格或Tab分割
            String[] tokens = line.split("\\s+");
            if (tokens.length == 2) {
                String child = tokens[0];
                String parent = tokens[1];

                // 1. 以父母为Key，发送孩子信息
                context.write(new Text(parent), new Text("child#" + child));
                
                // 2. 以孩子为Key，发送父母信息
                context.write(new Text(child), new Text("parent#" + parent));
            }
        }
    }

    // Reducer 阶段：接收同一个人的所有关系，进行配对
    public static class GrandReducer extends Reducer<Text, Text, Text, NullWritable> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            List<String> children = new ArrayList<>();
            List<String> grandparents = new ArrayList<>();

            // 分类收集：哪些是key的孩子，哪些是key的父母
            for (Text val : values) {
                String str = val.toString();
                if (str.startsWith("child#")) {
                    children.add(str.substring(6)); // 取出 child# 后面的名字
                } else if (str.startsWith("parent#")) {
                    grandparents.add(str.substring(7)); // 取出 parent# 后面的名字
                }
            }

            // 双重循环配对：孩子 x 父母 = 祖孙
            for (String child : children) {
                for (String gp : grandparents) {
                    context.write(new Text(child + "\t" + gp), NullWritable.get());
                }
            }
        }
    }

    // Driver 阶段
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Grand Parent Find");
        
        job.setJarByClass(GrandParent.class);
        job.setMapperClass(GrandMapper.class);
        job.setReducerClass(GrandReducer.class);
        
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);
        
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}