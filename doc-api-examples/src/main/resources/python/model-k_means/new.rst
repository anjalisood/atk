
Consider the following model trained and tested on the sample data set in *frame* 'frame'.

<hide>
>>> import trustedanalytics as ta
>>> ta.connect()
-etc-
>>> frame = ta.Frame(ta.UploadRows([[2,"ab"],[1,"cd"],[7,"ef"],[1,"gh"],[9,"ij"],[2,"kl"],[0,"mn"],[6,"op"],[5,"qr"]],
...                               [("data", ta.float64),("name", str)]))
-etc-

</hide>
Consider the following frame containing two columns.

>>> frame.inspect()
[#]  data  name
===============
[0]   2.0  ab
[1]   1.0  cd
[2]   7.0  ef
[3]   1.0  gh
[4]   9.0  ij
[5]   2.0  kl
[6]   0.0  mn
[7]   6.0  op
[8]   5.0  qr
>>> model = ta.KMeansModel()
<progress>
>>> train_output = model.train(frame, ["data"], [1], 3)
<progress>
<skip>
>>> train_output
{u'within_set_sum_of_squared_error': 5.3, u'cluster_size': {u'Cluster:1': 5, u'Cluster:3': 2, u'Cluster:2': 2}}
</skip>
>>> train_output.has_key('within_set_sum_of_squared_error')
True
>>> predicted_frame = model.predict(frame, ["data"])
<progress>
>>> predicted_frame.column_names
[u'data', u'name', u'distance_from_cluster_1', u'distance_from_cluster_2', u'distance_from_cluster_3', u'predicted_cluster']
>>> model.publish()
<progress>
<skip>
"hdfs://HOST:PORT/user/atkuser/models_5a4f72ab41c54c8c85cfc981c2952905.tar"
</skip>
<skip>
>>> import requests
>>> headers = {'Content-type': 'application/json', 'Accept': 'application/json,text/plain'}

# posting a request to get the metadata about the model
>>> r =requests.get('http://my-svm-model.demotrustedanalytics.com/v2/metadata')
>>> r.text
u'{"model_details":{"model_type":"LibSvm Model","model_class":"org.trustedanalytics.atk.scoring.models.LibSvmModel","model_reader":"org.trustedanalytics.atk.scoring.models.LibSvmModelReaderPlugin","custom_values":{}},"input":[{"name":"tr_row","value":"Double"},{"name":"tr_col","value":"Double"}],"output":[{"name":"tr_row","value":"Double"},{"name":"tr_col","value":"Double"},{"name":"Prediction","value":"Double"}]}'

# Posting a request to version 1 of Scoring Engine supporting strings for requests and response:
>>> r = requests.post('http://my-svm-model.demotrustedanalytics.com/v1/score?data=2,17,-6', headers=headers)
>>> r.text
u'-1.0'

# Posting a request to version 1 with multiple records to score:
>>> r = requests.post('http://my-svm-model.demotrustedanalytics.com/v1/score?data=2,17,-6&data=0,0,0', headers=headers)
>>> r.text
u'-1.0,1.0'

# Posting a request to version 2 of Scoring Engine supporting Json for requests and responses. In the following example, 'tr_row' and 'tr_col' are the names of the observation columns that the model was trained on:
>>> r = requests.post("http://my-svm-model.demotrustedanalytics.com/v2/score", json={"records": [{"tr_row": 1.0, "tr_col": 2.6}]})
>>> r.text
u'{"data":[{"tr_row":1.0,"tr_col":2.6,"Prediction":-1.0}]}'

# posting a request to version 2 with multiple records to score:
>>> r = requests.post("http://my-svm-model.demotrustedanalytics.com/v2/score", json={"records": [{"tr_row": 1.0, "tr_col": 2.6},{"tr_row": 3.0, "tr_col": 0.6} ]})
>>> r.text
u'{"data":[{"tr_row":1.0,"tr_col":2.6,"Prediction":-1.0},{"tr_row":3.0,"tr_col":0.6,"Prediction":-1.0}]}'
</skip>
