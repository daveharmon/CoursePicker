to compile, cd to src directory and run:
	javac parser/MedianParser.java -classpath ~/Desktop/Developer/elasticsearch-1.5.2/lib/elasticsearch-1.5.2.jar 
to run:
	java parser.MedianParser /path/to/file

curl -XPOST http://localhost:9200/classes/classes/_mapping -d'
{
	"classes": {
		"properties": {
			"course": {
				"type": "string",
				"index": "not_analyzed"
			},
			"median": {
				"type": "string",
				"index": "not_analyzed"
			}
		}
	}
}'

curl -XPOST http://localhost:9200/_shutdown

curl -XDELETE http://localhost:9200/classes/classes
