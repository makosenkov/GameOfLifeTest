import java.io.* ;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *   Invoke one of the Life implementations against a particular
 *   input pattern.  The input pattern is read from standard input.
 *
 *   Usage:  java Driver <classname> <pattern.rle
 *
 *   Warning:  runs forever; it may put your machine into swap
 *   or generate huge output files, so be prepared to stop it with
 *   a control-C.
 */
public class Driver {
   /**
    *   If something is wrong with the command line we complain here.
    */
   static void usage(String s) {
      System.out.println(s) ;
      System.out.println("Usage:  java Driver <classname> <pattern.rle") ;
      System.exit(10) ;
   }
   /**
    *   Read a pattern in RLE format.  This format uses 'b' for a blank,
    *   'o' for a live cell, '$' for a new line, and integer repeat
    *   counts before any of this.  It also contains a leading line
    *   containing the x and y location of the upper right.  This is
    *   not a full-fledged reader but it will read the patterns we
    *   include.  Any ! outside a comment will terminate the pattern.
    */
   static void readPattern(List<UniverseInterface> maps) throws Exception {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in)) ;
      String inputLine = null ;
      int x = 0, y = 0 ;              // current location
      int paramArgument = 0 ;         // our parameter argument so far
      while ((inputLine = br.readLine()) != null) {
         // ignore lines that start with 'x' or '#'
         if (inputLine.startsWith("x") || inputLine.startsWith("#"))
            continue ;
         inputLine = inputLine.trim() ;
         for (int i=0; i<inputLine.length(); i++) {
            char c = inputLine.charAt(i) ;
            int param = (paramArgument == 0 ? 1 : paramArgument) ;
            if (c == 'b') {
               x += param ;
               paramArgument = 0 ;
            } else if (c == 'o') {
               while (param-- > 0)
                  //univ.setBit(x++, y) ;
               paramArgument = 0 ;
            } else if (c == '$') {
               y += param ;
               x = 0 ;
               paramArgument = 0 ;
            } else if ('0' <= c && c <= '9') {
               paramArgument = 10 * paramArgument + c - '0' ;
            } else if (c == '!') {
               return ;
            } else {
               usage("In the input, I saw the character " + c +
                " which is illegal in the RLE subset I know how to handle.") ;
            }
         }
      }
   }
   /**
    *   Main picks up the class to use for the UniverseImplementation
    *   from the command line, loads and instantiates that class,
    *   parses the RLE from the standard input, setting bits as it
    *   goes, and then runs the Universe forever.
    */

   public static final Long ITERATIONS = 50000L;
   public static final Long PRINT_EVERY = 1500L;
   public static final Integer SEED = new Random().nextInt(200000);
   public static final Integer MAP_MIN_SIDE = 30;
   public static final Integer MAP_MAX_SIDE = 50;
   public static final Integer MAPS_QUANTITY = 10;

   public static void main(String[] args) throws Exception {

      Random random = new Random(SEED);

      List<Boolean[][]> maps = new ArrayList<>();

      for (int i = MAPS_QUANTITY; i > 0; i--) {
         System.out.println("Generating map : " + i + " left");
         maps.add(generateMap(random));
      }


      testDrive(maps, ITERATIONS);

   }

   private static Boolean[][] generateMap(Random random) {
      int width = MAP_MIN_SIDE + random.nextInt(MAP_MAX_SIDE - MAP_MIN_SIDE);
      int height = MAP_MIN_SIDE + random.nextInt(MAP_MAX_SIDE - MAP_MIN_SIDE);

      Boolean[][] map = new Boolean[height][width];

      for (int x = 0; x < width; x++){
         for (int y = 0; y < height; y++){
            map[y][x] = random.nextBoolean();
         }
      }

      System.out.println("Generating map : DONE");
      return map;
   }

   private static long checkTime(Boolean[][] map, UniverseInterface univ, long iters) {
//      System.out.println("Testing map for one ALG");
      loadMap(map, univ);
      long timeCurrent = System.currentTimeMillis();
      for (long i = 1; i <= iters; i++) {
         univ.runStep();
         if (i % PRINT_EVERY == 0)
            System.out.println(i + " / " + iters);
      }
//      System.out.println("Testing map : DONE");
      return System.currentTimeMillis() - timeCurrent;
   }

   private static void testDrive(List<Boolean[][]> maps, long iterations){
      for (Boolean[][] map: maps){
         testDriveMap(map, iterations);
      }
   }

   private static void testDriveMap(Boolean[][] map, long iterations) {

      //UniverseInterface univTree = new TreeUniverse();
      UniverseInterface univHash = new HashLifeTreeUniverse();
      UniverseInterface univCanon = new CanonicalTreeUniverse();
      UniverseInterface univMemorized = new MemoizedTreeUniverse();

      //long timeTree = checkTime(map, univTree, iterations);
      long timeHash = checkTime(map, univHash, iterations);
      long timeCanon = checkTime(map, univCanon, iterations);
      long timeMemo = checkTime(map, univMemorized, iterations);

      System.out.println("------------------");
      System.out.println("Map size: " + map[0].length + " x " + map.length);
      System.out.println("Iterations: " + iterations);
      System.out.println();
      //System.out.println("Tree = " + timeTree);
      System.out.println("Canon = " + timeCanon);
      System.out.println("Memo = " + timeMemo);
      System.out.println("Hash = " + timeHash);

   }

   private static void loadMap(Boolean[][] map, UniverseInterface univ) {

//      System.out.println("Loading map to UNIV");

      for (int x = 0; x < map[0].length; x++){
         for (int y = 0; y < map.length; y++){
            if (map[y][x])
               univ.setBit(x, y);
         }
      }


//      System.out.println("Loading map : DONE");

   }

}
