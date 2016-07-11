package net.iowntheinter.util

import groovy.json.JsonOutput
import io.vertx.core.eventbus.Message
import wagu.*;

/**
 * Created by g on 6/21/16.
 */
class displayOutput {
    def String outputType;
    displayOutput( ){
        displayOutput('json')
    }
    displayOutput(String type){
        outputType = type
    }


    void display(Map d){
        switch (outputType){
            case "json":
                displayJson(d);
                break;
            case "table":
                displayTable(d)
                break;
        }

    }
    void displayTable(Map input) {
        List<String> headersList = new ArrayList<String>()
        input['cols'].each { String k ->
            headersList.add(k)
        }
        List<List<String>> rowsList = new ArrayList()
        input['data'].each {  k,  v ->
          rowsList.add(Arrays.asList(k) + v)
        }
        def width = new Integer(System.getenv("COLUMNS"))
        Board board = new Board(width );
        Table datat = new Table(board, width - 2 ,  headersList,rowsList)
        Block header = new Block(board, width - 2 , 1, input['header'] as String)
        board.setInitialBlock(header.allowGrid(false).setBlockAlign(Block.BLOCK_LEFT).setDataAlign(Block.DATA_CENTER))
        board.appendTableTo(0,Board.APPEND_BELOW,datat)

        String ts = board.build().getPreview()
        println(ts)
    }
    void displayJson(Map input) {
        println(JsonOutput.prettyPrint(JsonOutput.toJson(input)))
    }

}


