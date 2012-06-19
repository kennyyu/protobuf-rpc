package me.kennyyu.rpc;

import me.kennyyu.rpc.proto.Cheese;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        Cheese foo = Cheese.getDefaultInstance();
        System.out.println(foo);
    }
}
