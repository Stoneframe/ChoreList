//package stoneframe.chorelist;
//
//import static org.junit.Assert.assertEquals;
//
//import org.junit.Test;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import stoneframe.chorelist.model.chores.Chore;
//import stoneframe.chorelist.model.chores.ChoreManager;
//import stoneframe.chorelist.model.chores.choreselectors.SimpleChoreSelector;
//import stoneframe.chorelist.model.chores.efforttrackers.SimpleEffortTracker;
//
//public class ChoreManagerTest
//{
//    @Test
//    public void two_chores_scheduled_for_before_now()
//    {
//        Chore chore1 = new Chore("Chore1", 5, 5, TestUtils.MOCK_TODAY.minusDays(2), 1, Chore.DAYS);
//        Chore chore2 = new Chore("Chore2", 3, 8, TestUtils.MOCK_TODAY.minusDays(1), 1, Chore.DAYS);
//
//        ChoreManager choreManager = new ChoreManager(
//            new SimpleEffortTracker(15),
//            new SimpleChoreSelector());
//
//        choreManager.addChore(chore1);
//        choreManager.addChore(chore2);
//
//        List<Chore> expected = new LinkedList<>();
//        expected.add(chore2);
//        expected.add(chore1);
//
//        List<Chore> actual = choreManager.getChores(TestUtils.MOCK_TODAY);
//
//        assertEquals(expected, actual);
//    }
//}
