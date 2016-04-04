/**
 *  Copyright (c) 2015 Intel Corporation 
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.trustedanalytics.atk.scoring.interfaces

import scala.concurrent.Future
/**
 * Base interface for a Model loader.
 */
trait Model {

  /**
   * Called for scoring
   */
  def score(row: Array[Any]): Array[Any]

  def scoreToMap(row: Array[Any]): Map[String, Any] = {
    score(row).zip(output()).map(combined => (combined._2.name, combined._1)).toMap
  }

  def input(): Array[Field]

  def output(): Array[Field]

  def modelMetadata(): ModelMetaDataArgs
}

