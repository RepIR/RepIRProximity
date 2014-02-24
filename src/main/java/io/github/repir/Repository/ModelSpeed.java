package io.github.repir.Repository;

import io.github.repir.Repository.StoredDynamicFeature;
import io.github.repir.Repository.Repository;
import java.util.ArrayList;
import io.github.repir.Repository.ModelSpeed.File;
import io.github.repir.Repository.ModelSpeed.Record;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordHeaderData;
import io.github.repir.tools.Content.RecordHeaderDataRecord;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;
import io.github.repir.tools.Lib.PrintTools;

/**
 * 
 * @author jer
 */
public class ModelSpeed extends StoredDynamicFeature<File, Record> {

   public static Log log = new Log(ModelSpeed.class);
   
   public ModelSpeed(Repository repository) {
      super(repository);
   }

   @Override
   public File createFile(Datafile df) {
      return new File(df);
   }
   
   public Record newRecord( String strategy, int query ) {
      return new Record(strategy, query);
   }

   public ArrayList<Record> readAll() {
      ArrayList<Record> records = new ArrayList<Record>();
      openRead();
      records.addAll(getFile().getKeys());
      return records;
   }

   public class File extends RecordHeaderData<Record> {

      public StringField strategy = this.addString("strategy");
      public IntField query = this.addInt("query");
      public DoubleField time = this.addDouble("time");

      public File(Datafile df) {
         super(df);
      }

      @Override
      public Record newRecord() {
         return new Record();
      }
      
      public Record newRecord( String strategy, int query ) {
         return new Record( strategy, query );
      }
   }

   public class Record implements RecordHeaderDataRecord<File> {
      public String strategy;
      public int query;
      public double time = -1;
      
      public Record() {}
      
      public Record( String strategy, int query ) {
         this.strategy = strategy;
         this.query = query;
      }
      
      @Override
      public String toString() {
         return PrintTools.sprintf("%3d %.3f %s", query, time, strategy); 
      }
      
      @Override
      public int hashCode() {
         int hash = 31;
         hash = MathTools.combineHash(hash, strategy.hashCode());
         hash = MathTools.combineHash(hash, query);
         return MathTools.finishHash(hash);
      }

      @Override
      public boolean equals(Object r) {
         if (r instanceof Record) {
            Record record = (Record)r;
            return strategy.equals(record.strategy) && query == record.query;
         }
         return false;
      }

      public void write(File file) {
         file.strategy.write( strategy );
         file.query.write( query );
         file.time.write(time);
      }

      public void read(File file) {
         strategy = file.strategy.value;
         query = file.query.value;
         time = file.time.value;
      }

      public void convert(RecordHeaderDataRecord record) {
         Record r = (Record)record;
         r.strategy = strategy;
         r.query = query;
         r.time = time;
      }
   }
   
   public Record read( String strategy, int query ) {
      this.openRead();
      Record s = (Record)newRecord( strategy, query );
      Record r = (Record) find(s);
      return r;
   }
   
   public Record read( Record record ) {
      this.openRead();
      Record found = (Record) find(record);
      return (found == null)?record:found;
   }
}
