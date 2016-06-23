package net.iowntheinter.vertx.util

import wagu.*;

/**
 * Created by g on 6/21/16.
 */
class displayTables {


    void displayTable(Map input) {
        List<String> headersList = new ArrayList<String>()
        input['cols'].each { String k ->
            headersList.add(k)
        }
        List<List<String>> rowsList = new ArrayList()
        input['data'].each {  k,  v ->
          rowsList.add(Arrays.asList(k) + v)
        }
        Board board = new Board(80);
        Table datat = new Table(board, 72,  headersList,rowsList)
        Block header = new Block(board, 72, 1, input['header'] as String)
        board.setInitialBlock(header.allowGrid(false).setBlockAlign(Block.BLOCK_LEFT).setDataAlign(Block.DATA_CENTER))
        board.appendTableTo(0,Board.APPEND_BELOW,datat)

        String ts = board.build().getPreview()
        println(ts)
    }
}


