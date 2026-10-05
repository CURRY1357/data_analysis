import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class MergeDedup {

    // 1. Mapper 阶段：读取每一行，作为 Key 发出去
    public static class DedupMapper extends Mapper<Object, Text, Text, NullWritable> {
        private Text line = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            // 去除首尾空格，将整行内容作为 Key（例如 "20170101 x"）
            line.set(value.toString().trim());
            // Value 设置为空（NullWritable），因为我们只关心去重
            context.write(line, NullWritable.get());
        }
    }

    // 2. Reducer 阶段：框架会自动把相同的 Key 分到一组
    public static class DedupReducer extends Reducer<Text, NullWritable, Text, NullWritable> {
        public void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            // 不管这组里有多少个相同的行，我们只输出一次！
            context.write(key, NullWritable.get());
        }
    }

    // 3. Driver 阶段：配置并提交任务
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Merge and Dedup");
        
        job.setJarByClass(MergeDedup.class);
        job.setMapperClass(DedupMapper.class);
        job.setReducerClass(DedupReducer.class);
        
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);
        
        // 设置输入和输出路径（由运行时参数传入）
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}