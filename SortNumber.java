import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class SortNumber {

    // 1. Mapper 阶段：读取每行整数，作为 Key 发出，Value 为空
    public static class SortMapper extends Mapper<Object, Text, IntWritable, NullWritable> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            // 将文本解析为整数，框架会自动按整数大小进行排序
            int num = Integer.parseInt(value.toString().trim());
            context.write(new IntWritable(num), NullWritable.get());
        }
    }

    // 2. Reducer 阶段：因为只有一个 Reducer，它会按顺序拿到排好序的 Key
    public static class SortReducer extends Reducer<IntWritable, NullWritable, Text, NullWritable> {
        private int rank = 1; // 排名计数器

        public void reduce(IntWritable key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            // 输出格式为： "排名 数字"
            String result = rank + " " + key.get();
            context.write(new Text(result), NullWritable.get());
            rank++; // 排名+1
        }
    }

    // 3. Driver 阶段
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Number Sort");
        
        job.setJarByClass(SortNumber.class);
        job.setMapperClass(SortMapper.class);
        job.setReducerClass(SortReducer.class);
        
        // 设置 Map 的输出类型
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(NullWritable.class);
        
        // 设置最终输出的类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        // ★★★ 核心：强制只用一个 Reducer，保证全局排名连续 ★★★
        job.setNumReduceTasks(1);
        
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}