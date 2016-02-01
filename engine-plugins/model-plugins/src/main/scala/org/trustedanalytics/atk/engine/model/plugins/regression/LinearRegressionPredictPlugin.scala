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
package org.trustedanalytics.atk.engine.model.plugins.regression

import org.apache.spark.frame.FrameRdd
import org.trustedanalytics.atk.domain.CreateEntityArgs
import org.trustedanalytics.atk.domain.frame.{ FrameEntity, FrameReference }
import org.trustedanalytics.atk.engine.frame.SparkFrame
import org.trustedanalytics.atk.engine.model.Model
import org.trustedanalytics.atk.engine.plugin.{ Invocation, ApiMaturityTag, SparkCommandPlugin }
import org.trustedanalytics.atk.domain.DomainJsonProtocol._
import org.apache.spark.ml.atk.plugins.MLJsonProtocol._
//Implicits needed for JSON conversion
import spray.json._

class LinearRegressionPredictPlugin extends SparkCommandPlugin[LinearRegressionPredictArgs, FrameReference] {
  /**
   * The name of the command.
   *
   * The format of the name determines how the plugin gets "installed" in the client layer
   * e.g Python client via code generation.
   */
  override def name: String = "model:linear_regression_ml/predict"

  override def apiMaturityTag = Some(ApiMaturityTag.Alpha)

  /**
   * Number of Spark jobs that get created by running this command
   * (this configuration is used to prevent multiple progress bars in Python client)
   */
  override def numberOfJobs(arguments: LinearRegressionPredictArgs)(implicit invocation: Invocation) = 9

  /**
   * Run MLLib's LinearRegressionWithSGD() on the training frame and create a Model for it.
   *
   * @param invocation information about the user and the circumstances at the time of the call,
   *                   as well as a function that can be called to produce a SparkContext that
   *                   can be used during this invocation.
   * @param arguments user supplied arguments to running this plugin
   * @return a value of type declared as the Return type.
   */
  override def execute(arguments: LinearRegressionPredictArgs)(implicit invocation: Invocation): FrameReference = {
    val model: Model = arguments.model
    val frame: SparkFrame = arguments.frame
    val dataFrame = frame.rdd.toDataFrame

    val linRegJsObject = model.dataOption.getOrElse(throw new RuntimeException("This model has not be trained yet. Please train before trying to predict"))
    val linRegData = linRegJsObject.convertTo[LinearRegressionData]
    val linRegModel = linRegData.linRegModel

    //val linRegColumn = arguments.observationColumns.getOrElse(linRegData.observationColumns)
    linRegModel.setFeaturesCol("features")
    val prediction = linRegModel.transform(dataFrame).cache()
    val predictionRdd = FrameRdd.toFrameRdd(prediction)

    engine.frames.tryNewFrame(CreateEntityArgs(description = Some("created by LinearRegression's predict operation"))) {
      newPredictedFrame: FrameEntity =>
        newPredictedFrame.save(predictionRdd)
    }
  }
}