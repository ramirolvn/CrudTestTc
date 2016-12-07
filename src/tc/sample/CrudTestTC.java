package tc.sample;

import litebase.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.util.*;

public class CrudTestTC extends MainWindow implements Grid.DataSource
{
   
   LitebaseConnection driver;
   private TabbedContainer container;
   private Grid grid;
   private Editit edit;
   ResultSet activeRS;
   PreparedStatement psList;

   static
   {
      Settings.useNewFont = true;
   }
   
   
   public CrudTestTC()
   {
      super("CrudTestTC", TAB_ONLY_BORDER);
      if (Settings.onJavaSE)
         totalcross.sys.Settings.showDesktopMessages = false;
      setUIStyle(Settings.Android);
      driver = LitebaseConnection.getInstance("ABok");
   }

   
   public void initUI()
   {
      createTables(); 

      String[] tpCaptions = {"Lista", "Editar"};
      add(container = new TabbedContainer(tpCaptions));
      container.setBorderStyle(Window.NO_BORDER);
      container.setRect(getClientRect());
      edit = new Editit();
      edit.book = this;
      container.setContainer(1, edit);

      String[] gridCaptions = {"id", "Nome", "Endereço", "Telefone", "Data de Nascimento", "Salário", "Est.Civil", "Gênero", "Última Modificação"};
      int gridWidths[] =
      {
         0,
         fm.stringWidth("aaaaaaaaaa"),
         fm.stringWidth("aaaaaaaaaaaaaaa"), 
         fm.stringWidth(edit.edPhone.getMask()),
         fm.stringWidth(edit.edBirth.getMask()),
         fm.stringWidth(edit.edSalary.getMask()), 
         fm.stringWidth("aaaaaaaaaaaaaaaaa"), 
         1, 
         fm.stringWidth("99/99/9999 99:99:99")
      };
          
      int gridAligns[] = {LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT};
      container.setContainer(0, grid = new Grid(gridCaptions, gridWidths, gridAligns, false));
      
      invalidateRS();
   }
   
   
   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            if (event.target == container)
               switch (container.getActiveTab())
               {
                  case 0: 
                     invalidateRS(); 
                     break;
                  case 1: 
                     String[] item = grid.getSelectedItem();
                     if (item == null)
                        edit.clear();
                     else
                     {
                        try
                        {
                           edit.rowId = Convert.toInt(item[0]);
                        }
                        catch (InvalidNumberException exception) {}
                        edit.show();
                     }
               }
      }
   }

   
   public String[][] getItems(int startIndex, int count)
   {
      if (activeRS != null)
      {
         activeRS.absolute(startIndex);
         
         String[][] matrix = activeRS.getStrings(count);
         int size = matrix.length,
             date,
             time;
         long dateTime;
         Date tempDate = edit.tempDate;
         Time tempTime = edit.tempTime;
         StringBuffer sb = edit.buffer;
         
         try
         {
            while (--size >= 0)
            {
               date = Convert.toInt(matrix[size][4]);
               tempDate.set(date % 100, (date /= 100) % 100, date / 100);
               matrix[size][4] = edit.tempDate.toString(); 
               matrix[size][7] = matrix[size][7].equals("1")? "M" : "F";  
               dateTime = Convert.toLong(matrix[size][8]);
               time = (int)(dateTime % 1000000);
               date = (int)(dateTime / 1000000);
               tempDate.set(date % 100, (date /= 100) % 100, date / 100);
               tempTime.second = time % 100;
               tempTime.minute = (time /= 100) % 100;
               tempTime.hour = time / 100;
               sb.setLength(0);
               matrix[size][8] = sb.append(tempDate).append(' ').append(tempTime).toString();
            }
         }
         catch (InvalidNumberException exception) {}
         catch (InvalidDateException exception) {}
         return matrix;
      }
      return null;
   }

   
   void invalidateRS()
   {
      if (activeRS != null)
         activeRS.close();

      if (psList != null)
      {
         (activeRS = psList.executeQuery()).setDecimalPlaces(6, 2);
         if (activeRS.first()) 
            grid.setDataSource(this, activeRS.getRowCount());
         else
         {
            grid.removeAllElements();
            activeRS.close();
            activeRS = null;
         }
      }
   }

   
   private void createTables()
   {
      try
      {
         
         if (!driver.exists("bookentry"))
         {
            driver.execute("create table bookentry(name char(30), address char(50), phone char(20), birthday int, salary float, married char(30), " 
                                                                                 + "gender short, lastUpdated long)");
            driver.execute("CREATE INDEX IDX_0 ON bookentry(rowid)"); 
         }    
      }
      catch (AlreadyCreatedException exception) {}

      psList = driver.prepareStatement("select rowid, name, address, phone, birthday, salary, married, gender, lastUpdated from " 
                                                                                                                                  + "bookentry");   
   }  
}
