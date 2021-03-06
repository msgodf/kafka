/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.utils

import java.util.concurrent._
import scala.math._

class DelayedItem[T](val item: T, delay: Long, unit: TimeUnit) extends Delayed with Logging {

  val createdMs = SystemTime.milliseconds
  val delayMs = {
    val given = unit.toMillis(delay)
    if (given < 0 || (createdMs + given) < 0) (Long.MaxValue - createdMs)
    else given
  }

  def this(item: T, delayMs: Long) = 
    this(item, delayMs, TimeUnit.MILLISECONDS)

  /**
   * The remaining delay time
   */
  def getDelay(unit: TimeUnit): Long = {
    val elapsedMs = (SystemTime.milliseconds - createdMs)
    unit.convert(max(delayMs - elapsedMs, 0), unit)
  }
    
  def compareTo(d: Delayed): Int = {
    val delayed = d.asInstanceOf[DelayedItem[T]]
    val myEnd = createdMs + delayMs
    val yourEnd = delayed.createdMs + delayed.delayMs

    if(myEnd < yourEnd) -1
    else if(myEnd > yourEnd) 1
    else 0
  }
  
}
