package tc.sample;

import litebase.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.*;
import totalcross.ui.gfx.Color;
import totalcross.ui.media.Sound;
import totalcross.util.*;

class Editit extends Container
{
   private Edit edName;
   private Edit edAddress;
   Edit edPhone;
   Edit edBirth;
   Edit edSalary;
   private Radio rdMale;
   private Radio rdFemale;
   private RadioGroupController rgGender;
   private Check chMarried;
   private PushButtonGroup pbgAction;
   private Button btnOk;
   private Label lbStatus;
   CrudTestTC book;
   int rowId;
   private LitebaseConnection driver;
   private PreparedStatement psDelete;
   private PreparedStatement psInsert;
   private PreparedStatement psUpdate;
   private PreparedStatement psSelect;
   Date tempDate = new Date();
   Time tempTime = new Time();
   StringBuffer buffer = new StringBuffer(100);

   
   public void initUI()
   {
      Label labelAux = new Label("Data de Nascimento:");
      Edit editAux = edBirth = new Edit("31/12/2016") ;
      int xx = labelAux.getPreferredWidth() + 5; 
      
      
      add(new Label("Nome:"),LEFT + 1, AFTER + 2);
      add(edName = new Edit(""), xx, SAME - 1);
      edName.setMaxLength(30);

      
      add(new Label("Endereço:"),LEFT + 1,AFTER + 1);
      add(edAddress = new Edit(""), xx, SAME - 1);
      edAddress.setMaxLength(50);

      
      add(new Label("Telefone:"),LEFT + 1,AFTER + 1);
      add(edPhone = new Edit("9999-9999"), xx, SAME - 1);
      edPhone.setMaxLength(20);
      edPhone.setMode(Edit.CURRENCY);

      
      add(labelAux, LEFT + 1, AFTER + 1);
      add(editAux = edBirth = new Edit("31/12/2016"), xx, SAME - 1);
      editAux.setMode(Edit.DATE);
      editAux.setMaxLength(10);

      
      add(new Label("Salário"), LEFT, AFTER + 1);
      add(edSalary = new Edit("9999999.99"), xx, SAME - 1);
      edSalary.setMode(Edit.CURRENCY);
      
      
      add(new Label("Gênero"), LEFT, AFTER + 1);
      add(rdMale = new Radio("M", rgGender = new RadioGroupController()), xx, SAME - 1);
      add(rdFemale = new Radio("F", rgGender), AFTER + 2, SAME);
      rdMale.setChecked(true);

      
      add(chMarried = new Check("Casado?"), xx, AFTER + 1);

      
      add(pbgAction = new PushButtonGroup(new String[]{"Adicionar", "Editar", "Deletar", "Limpar"}, false, -1, 0, 4, 0, false, PushButtonGroup.NORMAL), 
                                                                                                                      LEFT, AFTER + 2);
      add(btnOk = new Button(" Ok "), RIGHT, SAME);

      
      (labelAux = lbStatus = new Label("", CENTER)).setInvert(true);
      labelAux.setForeColor(Color.brighter(getForeColor()));
      add(labelAux, LEFT, BOTTOM);
     
      driver = book.driver;
      psDelete = driver.prepareStatement("delete bookentry where rowid = ?");
      psInsert = driver.prepareStatement("insert into bookentry values (?, ?, ?, ?, ?, ?, ?, ?)");
      psUpdate = driver.prepareStatement("update bookentry set name = ?, address = ?, phone = ?, birthday = ?, salary = ?, married = ?, gender = ?,"
                                                                                              + "lastUpdated = ? where rowid = ?");
      psSelect = driver.prepareStatement("select * from bookentry where rowid = ?");
   }

   
   private boolean verifyFields()
   {  
      StringBuffer sb = buffer;
      
      sb.setLength(0);
      if (edName.getText().length() == 0)
         sb.append("name |");   
      if (edAddress.getText().length() == 0)
         sb.append("address |");   
      if (edPhone.getText().length() == 0)
         sb.append("phone |");
      try
      {
         if (Convert.toDouble(edSalary.getText()) < 0)
            sb.append("salary |");
      } catch (InvalidNumberException exception)
      {
         sb.append("salary |");
      }
      try
      {
          tempDate.set(edBirth.getText(), Settings.dateFormat);
      } catch (InvalidDateException exception)
      {
         sb.append("birthday |");
      }

      if (sb.length() > 0) 
      {
         sb.setLength(sb.length() -1); 
         new MessageBox("Atenção","Você deve preencher os seguintes campos corretamente: |" + sb).popupNonBlocking();
         return false;
      }
      return true;
   }

   
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {   
         if (event.target == btnOk)
         {
            switch (pbgAction.getSelectedIndex())
            {
               case 0: 
                  if (verifyFields())
                     doInsertUpdate(true);
                  break;
               case 1: 
                  if (verifyFields())
                     if (rowId > 0)
                        doInsertUpdate(false);
                     else
                        Sound.beep();
                  break;
               case 2: 
                  if (rowId > 0) 
                     doDelete();
                  else
                     Sound.beep();
                  break;
               case 3: 
                  clear();
                  break;
               default:
                  Sound.beep();
            }
         }
      }
   }
   
   
   private void doInsertUpdate(boolean isInsert)
   {
      String name = edName.getText(),
             addr = edAddress.getText(),
             phone = edPhone.getText();
      		 String married = "";
      if (chMarried.isChecked() == true){
    	   married = "Casado";
      }
      else{
    	  married = "Solteiro";
      }
      int birth = -1,
          gender = rdMale.isChecked()? 1 : 0,
          rows = -1;
      double salary = 0;
      long lastUpdated = new Time().getTimeLong();
      PreparedStatement psAux;
      
      try
      {
         birth = tempDate.set(edBirth.getText(), Settings.dateFormat);
         salary = Convert.toDouble(edSalary.getText());
      }
      catch (Exception exception)
      {
         MessageBox.showException(exception, true);
      }
      
      if (isInsert)
         psAux = psInsert;
      else
         (psAux = psUpdate).setInt(8, rowId);
         
      psAux.setString(0, name);
      psAux.setString(1, addr);
      psAux.setString(2, phone);
      psAux.setInt(3, birth);
      psAux.setFloat(4, salary);
      psAux.setString(5, married);
      psAux.setShort(6, (short)gender);
      psAux.setLong(7, lastUpdated);
      rows = psAux.executeUpdate();
      if (rows == 1)
      {
         book.invalidateRS();
         clear();
      }
      else 
         Sound.beep();
   }

   
   private void doDelete()
   {
      psDelete.setInt(0, rowId);
      if (psDelete.executeUpdate() == 1)
      {
         book.invalidateRS();
         clear();
      }
      else 
         Sound.beep();
   }

   
   public void clear()
   {
      edName.setText("");
      edAddress.setText("");
      edPhone.setText("");
      edBirth.setText("");
      edSalary.setText("");
      chMarried.setChecked(false);
      rgGender.setSelectedItem(rdMale);
      pbgAction.setSelectedIndex(-1);
      lbStatus.setText("");
      rowId = -1;
   }

   
   public void show()
   {
      psSelect.setInt(0, rowId);
      ResultSet resultSet = psSelect.executeQuery();
      resultSet.next();
      edName.setText(resultSet.getString(1));
      edAddress.setText(resultSet.getString(2));
      edPhone.setText(resultSet.getString(3));

      int date = resultSet.getInt(4);
      try
      {
         tempDate.set(date % 100, (date /= 100) % 100, date / 100);
         edBirth.setText("" + tempDate);
      } 
      catch (InvalidDateException exception) {}

      edSalary.setText(Convert.toString(resultSet.getFloat(5), 2));
      if (resultSet.getString(6).equals("Casado")){
    	  chMarried.setChecked(true);
      }else{
    	  chMarried.setChecked(false);
      }


      String gender = resultSet.getString(7);
      rgGender.setSelectedItem(gender.charAt(0) == '1'? rdMale : rdFemale); 

      long dateTime = resultSet.getLong(8);
      int time = (int)(dateTime % 1000000);
      date = (int)(dateTime / 1000000);
      
      try
      {
         tempDate.set(date % 100, (date /= 100) % 100, date / 100);
         Time timeAux = tempTime;
         timeAux.second = time % 100;
         timeAux.minute = (time /= 100) % 100;
         timeAux.hour = time / 100;
         StringBuffer sb = buffer;
         sb.setLength(0);
         lbStatus.setText(sb.append("Last updated: ").append(tempDate).append(' ').append(timeAux).toString());
      } catch (InvalidDateException exception) {}

      pbgAction.setSelectedIndex(1);
   }
   
}
