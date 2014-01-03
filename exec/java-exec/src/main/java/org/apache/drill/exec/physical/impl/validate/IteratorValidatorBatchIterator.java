/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.physical.impl.validate;

import java.util.Iterator;

import org.apache.drill.common.expression.SchemaPath;
import org.apache.drill.exec.ops.FragmentContext;
import org.apache.drill.exec.record.BatchSchema;
import org.apache.drill.exec.record.RecordBatch;
import org.apache.drill.exec.record.TypedFieldId;
import org.apache.drill.exec.record.VectorWrapper;
import org.apache.drill.exec.record.WritableBatch;
import org.apache.drill.exec.record.selection.SelectionVector2;
import org.apache.drill.exec.record.selection.SelectionVector4;

public class IteratorValidatorBatchIterator implements RecordBatch{
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IteratorValidatorBatchIterator.class);

  private IterOutcome state = IterOutcome.NOT_YET;
  private final RecordBatch incoming;
  
  public IteratorValidatorBatchIterator(RecordBatch incoming){
    this.incoming = incoming;
  }
  
  private void validateReadState(){
    switch(state){
    case OK:
    case OK_NEW_SCHEMA:
      return;
    default:
      throw new IllegalStateException(String.format("You tried to do a batch data read operation when you were in a state of %s.  You can only do this type of operation when you are in a state of OK or OK_NEW_SCHEMA.", state.name()));
    }
  }
  
  @Override
  public Iterator<VectorWrapper<?>> iterator() {
    validateReadState();
    return incoming.iterator();
  }

  @Override
  public FragmentContext getContext() {
    return incoming.getContext();
  }

  @Override
  public BatchSchema getSchema() {
    validateReadState();
    return incoming.getSchema();
  }

  @Override
  public int getRecordCount() {
    validateReadState();
    return incoming.getRecordCount();
  }

  @Override
  public void kill() {
    incoming.kill();
  }

  @Override
  public SelectionVector2 getSelectionVector2() {
    validateReadState();
    return incoming.getSelectionVector2();
  }

  @Override
  public SelectionVector4 getSelectionVector4() {
    validateReadState();
    return incoming.getSelectionVector4();
  }

  @Override
  public TypedFieldId getValueVectorId(SchemaPath path) {
    validateReadState();
    return incoming.getValueVectorId(path);
  }

  @Override
  public VectorWrapper<?> getValueAccessorById(int fieldId, Class<?> clazz) {
    validateReadState();
    return incoming.getValueAccessorById(fieldId, clazz);
  }

  @Override
  public IterOutcome next() {
    if(state == IterOutcome.NONE ) throw new IllegalStateException("The incoming iterator has previously moved to a state of NONE. You should not be attempting to call next() again.");
    state = incoming.next();
    return state;
  }

  @Override
  public WritableBatch getWritableBatch() {
    validateReadState();
    return incoming.getWritableBatch();
  }

  @Override
  public void cleanup() {
    incoming.cleanup();
  }
}
