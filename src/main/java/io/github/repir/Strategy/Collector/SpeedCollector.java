package io.github.repir.Strategy.Collector;

import io.github.repir.Strategy.Collector.CollectorCachable;
import io.github.repir.Strategy.Collector.Collector;
import java.io.EOFException;
import java.util.HashMap;
import io.github.repir.Repository.ModelSpeed;
import io.github.repir.Repository.ModelSpeed.Record;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Collector.Collector;
import io.github.repir.Strategy.Collector.CollectorCachable;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.Content.StructureReader;
import io.github.repir.tools.Content.StructureWriter;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.RandomTools;

public class SpeedCollector extends CollectorCachable<Record> {

   public static Log log = new Log(SpeedCollector.class);
   static ModelSpeed dummyfeature = new ModelSpeed(null);
   HashMap<Record, Record> records = new HashMap<Record, Record>();

   public SpeedCollector() {
      super();
   }

   public SpeedCollector(RetrievalModel f) {
      super(f);
   }

   @Override
   public boolean equals(Object o) {
      return false;
   }

   @Override
   public int hashCode() {
      return 0;
   }

   public void startAppend() {
      sdf = getStoredDynamicFeature();
      for (Record r : ((ModelSpeed) sdf).readAll()) {
         records.put(r, r);
      }
      log.info("startAppend %s", records);
      sdf.openWrite();
   }

   @Override
   public void streamappend() {
      log.info("streamappend %s", this);
      for (Record r : records.values()) {
         log.info("writefile %s", r);
         sdf.write(r);
      }
   }

   @Override
   public void streamappend(Record record) {
      sdf.write(record);
   }

   @Override
   public void streamappend(CollectorCachable c) {
      for (Record r : records.values()) {


      ((SpeedCollector)c).streamappend(r);
      }
   }

   public Record createRecord() {
      ModelSpeed sdf = (ModelSpeed) getStoredDynamicFeature();
      Record r = (Record) sdf.newRecord();
      return r;
   }

   @Override
   public ModelSpeed getStoredDynamicFeature() {
      ModelSpeed sdf = (ModelSpeed) this.getRepository().getFeature("ModelSpeed");
      return sdf;
   }

   @Override
   public void aggregate(Collector collector) {
      for (Record r : ((SpeedCollector) collector).records.values()) {
         Record existingr = records.get(r);
         log.info("aggregate %s exist %s", r, existingr);
         if (existingr != null) {
            if (r.time < existingr.time) {
               existingr.time = r.time;
            }
         } else {
            records.put(r, r);
         }
      }
   }

   @Override
   public void aggregateDuplicatePartition(Collector collector) {
     aggregate(collector);
   }
   
   @Override
   public void writeKey(StructureWriter writer) {
   }

   @Override
   public void readKey(StructureReader reader) throws EOFException {
   }

   @Override
   public void writeValue(StructureWriter writer) {
      writer.writeC(records.size());
      for (Record r : records.values()) {
         writer.write(r.query);
         writer.write(r.strategy);
         writer.write(r.time);
         log.info("write %s", r);
      }
   }

   @Override
   public void readValue(StructureReader reader) throws EOFException {
      int count = reader.readCInt();
      for (int sense = 0; sense < count; sense++) {
         int query = reader.readInt();
         String strategy = reader.readString();
         double time = reader.readDouble();
         Record r = dummyfeature.newRecord(strategy, query);
         r.time = time;
         records.put(r, r);
         log.info("read %s", r);
      }
   }

   @Override
   public void reuse() {
      records = new HashMap<Record, Record>();
   }

   @Override
   public boolean reduceInQuery() {
      return false;
   }

   @Override
   public void setCollectedResults() {
   }

   @Override
   public void finishSegmentRetrieval() {
      Record r = createRecord();
      r.query = strategy.query.id;
      r.strategy = strategy.query.getStrategyClass();
      r.time = log.getTimePassed() / 1000;
      records.put(r, r);
      log.info("speed %d %s %f", r.query, r.strategy, r.time);
   }

   @Override
   public void prepareRetrieval() {
     log.startTime();
   }

   @Override
   protected void collectDocument(Document doc) {
   }

   @Override
   public void decode() {
   }
}
