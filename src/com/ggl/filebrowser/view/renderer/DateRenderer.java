package com.ggl.filebrowser.view.renderer;

import java.text.SimpleDateFormat;

import javax.swing.table.DefaultTableCellRenderer;
 
public class DateRenderer extends DefaultTableCellRenderer {
 
    private static final long   serialVersionUID    = 
            1112888720326043655L;
     
    private SimpleDateFormat formatter;
     
    public DateRenderer() {
        String pattern = "dd MMM yyyy";
        this.formatter = new SimpleDateFormat(pattern);
    }
     
    @Override
    protected void setValue(Object value) {
        setText((value == null) ? "" : formatter.format(value));
    }
 
}