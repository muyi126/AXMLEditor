import cn.wjdiankong.main.Main;
import org.junit.Test;

public class UnitTest {

    @Test
    public void testA() {
        Main.main(new String[]{
                "-tag",
                "-i",
                "D:\\Documents\\Project\\AXMLEditor\\xmltest\\insert.xml",
                "D:\\Desktop\\BuildTest\\decod_manifest\\app-debug\\AndroidManifest.xml",
                "D:\\Desktop\\BuildTest\\decod_manifest\\app-debug\\AndroidManifest_out.xml",
        });
    }

    @Test
    public void testInsertMetaData() {
        Main.main(new String[]{
                "-tag",
                "-i",
                "D:\\Documents\\Project\\AXMLEditor\\xmltest\\insert_meta-data.xml",
                "D:\\Desktop\\BuildTest\\decod_manifest\\app-debug\\AndroidManifest.xml",
                "D:\\Desktop\\BuildTest\\decod_manifest\\app-debug\\AndroidManifest_out.xml",
        });
    }


    @Test
    public void testInsertPermissionManifest() {
        Main.main(new String[]{
                "-tag",
                "-i",
                "D:\\Documents\\Project\\AXMLEditor\\xmltest\\insert_permission-data.xml",
                "D:\\Desktop\\BuildTest\\decod_manifest\\app-debug\\AndroidManifest.xml",
                "D:\\Desktop\\BuildTest\\decod_manifest\\app-debug\\AndroidManifest_out.xml",
        });
    }


    @Test
    public void testInsertTagManifest() {
        //-attr -i application package debuggable true AndroidManifest.xml AndroidManifest_out.xml
        Main.main(new String[]{
                "-attr",
                "-i",
                "manifest",
                "package",//修改Tag manifest 这个参数没有用
                "isFeatureSplit",
                "false",
                "http://sssss/tools",
                "D:\\Desktop\\BuildTest\\decod_manifest\\app-debug\\AndroidManifest.xml",
                "D:\\Desktop\\BuildTest\\decod_manifest\\app-debug\\AndroidManifest_out.xml",
        });
    }


}
