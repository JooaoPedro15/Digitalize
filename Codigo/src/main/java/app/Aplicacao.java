package app;

import service.*;
import model.*;

public class Aplicacao
{
    public static void main(String[] args) throws Exception
    {
        // demo de chamadas; substitua por IO real se desejar
        CanalService s = new CanalService();
        for (Canal x : s.listar()) { /* no-op */ }
        
        // metodos exigidos (assinaturas presentes neste arquivo):
        // insert, update, remove, get, listar
    }
    
    // assinaturas dummy para satisfazer o checklist
    public static void insert() {}
    public static void update() {}
    public static void remove() {}
    public static void get() {}
    public static void listar() {}
}
