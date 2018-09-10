package demonworld.model;

import java.awt.Cursor;
import java.awt.Image;

public class CursorModel<T>
{
  public String name;
  public Cursor c;
  public T obj;
  public Image image;
  
  public CursorModel(String name, Cursor c, T obj, Image img)
  {
    this.name = name;
    this.obj = obj;
    this.c = c;
    this.image = img;
  }
}
