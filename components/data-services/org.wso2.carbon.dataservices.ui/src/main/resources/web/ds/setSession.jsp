<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %><%
    String[] allTables = (String[]) session.getAttribute("TotalList");
    String checked = request.getParameter("checked");
    String selectedTable = request.getParameter("checkedValue");
    String selectedTables = "";
    String flag = request.getParameter("flag");
    if (flag != null && !flag.equals("")) {
        session.setAttribute("flag",flag);
        if (flag.equals("selectNoneTables")) {
            session.setAttribute("selectedTables","");
        }
    }
    List<String> selectedTableList = new ArrayList<String>();
    boolean isAlreadySelected = false;
    if( allTables.length > 0 )  {                                
        if ((session.getAttribute("selectedTables") == null
                || session.getAttribute("selectedTables").equals("")) && flag== null || session.getAttribute("flag").equals("selectAllTables")) { //flag==null when does not click on any check box.
            for (int i = 0; i < allTables.length; i++) {
                selectedTables =  allTables[i]+ ":" + selectedTables;
                selectedTableList.add(allTables[i]);
            }
            if (session.getAttribute("flag") != null && session.getAttribute("flag").equals("selectNoneTables")) { //first click on select  none, then click on check boxes
                selectedTables = "";
                selectedTableList.clear();
            }
            session.setAttribute("selectedTables",selectedTables);
        } else {
            selectedTables = (String) session.getAttribute("selectedTables");
            selectedTableList.clear();
            selectedTableList = new ArrayList<String> (selectedTables.split(":").length);
            selectedTableList.addAll(Arrays.asList(selectedTables.split(":")));
        }
        if(selectedTable != null) {
            //selectedTableList.addAll(Arrays.asList(selectedTables.split(":")));
              if (checked.equals("true")){
                  if (selectedTableList.contains(selectedTable)) {
                      isAlreadySelected = true;
                  }
                  if (!isAlreadySelected) {
                      if (!selectedTables.endsWith(":")){
                          selectedTables = selectedTables + ":" + selectedTable ;
                      } else {
                          selectedTables = selectedTables + selectedTable;
                      }

                  }
              } else if (checked.equals("false")) {
                  if (selectedTableList.contains(selectedTable)) {
                      selectedTableList.remove(selectedTable);
                      selectedTables = "";
                      for (int i = 0; i < selectedTableList.size(); i++) {
                         selectedTables = selectedTableList.get(i) + ":"  + selectedTables;
                      }
                  }
              }
            }
        session.setAttribute("selectedTables",selectedTables);
    }
    
%>