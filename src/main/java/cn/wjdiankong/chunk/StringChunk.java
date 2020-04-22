package cn.wjdiankong.chunk;

import java.util.ArrayList;

import cn.wjdiankong.main.Utils;

public class StringChunk {

    public byte[] type;
    public byte[] size;
    public byte[] strCount;
    public byte[] styleCount;
    public byte[] unknown;
    public byte[] strPoolOffset;
    public byte[] stylePoolOffset;
    public byte[] strOffsets;
    public byte[] styleOffsets;
    public byte[] strPool;
    public byte[] stylePool;

    public ArrayList<String> stringContentList;

    public byte[] getByte(ArrayList<String> strList) {

        Utils.writeDealInfo("strB 前 strList length : " + strList.size());
        byte[] strB = getStrListByte(strList);  // 这里出错
        Utils.writeDealInfo("strB length : " + strB.length);

        byte[] src = new byte[0];

        src = Utils.addByte(src, type);
        src = Utils.addByte(src, size);
        src = Utils.addByte(src, Utils.int2Byte(strList.size()));//字符个数
        src = Utils.addByte(src, styleCount);
        src = Utils.addByte(src, unknown);
        src = Utils.addByte(src, strPoolOffset);
        src = Utils.addByte(src, stylePoolOffset);

        byte[] strOffsets = new byte[0];
        ArrayList<String> convertList = convertStrList(strList);

        int len = 0;
        for (int i = 0; i < convertList.size(); i++) {
            strOffsets = Utils.addByte(strOffsets, Utils.int2Byte(len));
            len += (convertList.get(i).length() + 4);//这里的4包括字符串头部的字符串长度2个字节，和字符串结尾的2个字节
        }

        src = Utils.addByte(src, strOffsets);//写入string offsets值

        int newStyleOffsets = src.length;//写完strOffsets之后就是styleOffsets的值

        src = Utils.addByte(src, styleOffsets);//写入style offsets值

        int newStringPools = src.length;

        src = Utils.addByte(src, strB);//写入string pools

        src = Utils.addByte(src, stylePool); //写入style pools

        //因为strOffsets大小的改变，这里的styleOffsets也需要变动
        if (styleOffsets != null && styleOffsets.length > 0) {
            //只有style有效才能写入
            src = Utils.replaceBytes(src, Utils.int2Byte(newStyleOffsets), 28 + strList.size() * 4);
        }

        //因为strOffsets大小改变，这里的strPoolOffsets和stylePoolOffset也要变动
        src = Utils.replaceBytes(src, Utils.int2Byte(newStringPools), 20);//修改strPoolOffsets偏移值

        //对于String Chunk的大小必须是4的倍数，如果不是补齐，因为Chunk一定是2的倍数，所以只需要补齐2个字节即可
        if (src.length % 4 != 0) {
            src = Utils.addByte(src, new byte[]{0, 0});
        }

        //修改chunk最终的大小
        src = Utils.replaceBytes(src, Utils.int2Byte(src.length), 4);

        return src;
    }

    public int getLen() {
        return type.length + size.length + strCount.length + styleCount.length +
                unknown.length + strPoolOffset.length + stylePoolOffset.length +
                strOffsets.length + styleOffsets.length + strPool.length + stylePool.length;
    }

    public static StringChunk createChunk(byte[] byteSrc, int stringChunkOffset) {

        StringChunk chunk = new StringChunk();

        //String Chunk的标示
        chunk.type = Utils.copyByte(byteSrc, 0 + stringChunkOffset, 4);

        //String Size
        chunk.size = Utils.copyByte(byteSrc, 4 + stringChunkOffset, 4);
        int chunkSize = Utils.byte2int(chunk.size);

        //String Count
        chunk.strCount = Utils.copyByte(byteSrc, 8 + stringChunkOffset, 4);
        int chunkStringCount = Utils.byte2int(chunk.strCount);

        chunk.stringContentList = new ArrayList<String>(chunkStringCount);

        //Style Count
        chunk.styleCount = Utils.copyByte(byteSrc, 12 + stringChunkOffset, 4);
        int chunkStyleCount = Utils.byte2int(chunk.styleCount);

        //unknown
        chunk.unknown = Utils.copyByte(byteSrc, 16 + stringChunkOffset, 4);

        //这里需要注意的是，后面的四个字节是Style的内容，然后紧接着的四个字节始终是0，所以我们需要直接过滤这8个字节
        //String Offset 相对于String Chunk的起始位置0x00000008
        chunk.strPoolOffset = Utils.copyByte(byteSrc, 20 + stringChunkOffset, 4);

        //Style Offset
        chunk.stylePoolOffset = Utils.copyByte(byteSrc, 24 + stringChunkOffset, 4);

        //String Offsets
        chunk.strOffsets = Utils.copyByte(byteSrc, 28 + stringChunkOffset, 4 * chunkStringCount);

        //Style Offsets
        chunk.styleOffsets = Utils.copyByte(byteSrc, 28 + stringChunkOffset + 4 * chunkStringCount, 4 * chunkStyleCount);

        int stringContentStart = 8 + Utils.byte2int(chunk.strPoolOffset);

        //String Content
        byte[] chunkStringContentByte = Utils.copyByte(byteSrc, stringContentStart, chunkSize);

        /**
         * 在解析字符串的时候有个问题，就是编码：UTF-8和UTF-16,如果是UTF-8的话是以00结尾的，如果是UTF-16的话以00 00结尾的
         */
        //这里的格式是：偏移值开始的两个字节是字符串的长度，接着是字符串的内容，后面跟着两个字符串的结束符00
        byte[] firstStringSizeByte = Utils.copyByte(chunkStringContentByte, 0, 2);
        //一个字符对应两个字节
        int firstStringSize = Utils.byte2Short(firstStringSizeByte) * 2;
        byte[] firstStringContentByte = Utils.copyByte(chunkStringContentByte, 2, firstStringSize + 2);

        String firstStringContent = new String(firstStringContentByte);

        chunk.stringContentList.add(Utils.filterStringNull(firstStringContent));
        //将字符串都放到ArrayList中
        int endStringIndex = 2 + firstStringSize + 2;
        while (chunk.stringContentList.size() < chunkStringCount) {
            //一个字符对应两个字节，所以要乘以 2
            int stringSize = Utils.byte2Short(Utils.copyByte(chunkStringContentByte, endStringIndex, 2)) * 2;
//            Utils.writeDealInfo("Src length: " + chunkStringContentByte.length +"   start: " + endStringIndex + " len: " + stringSize);
            byte[] temp = Utils.copyByte(chunkStringContentByte, endStringIndex + 2, stringSize + 2);
//            Utils.writeDealInfo("temp length: " + temp.length);
            String str = new String(temp);
            chunk.stringContentList.add(Utils.filterStringNull(str));
            endStringIndex += (2 + stringSize + 2);
        }

        int len = 0;
        for (String str : chunk.stringContentList) {
            len += 2;
            len += str.length() * 2;
            len += 2;
        }
        chunk.strPool = Utils.copyByte(byteSrc, stringContentStart, len);
        int stylePool = stringContentStart + len;

        chunk.stylePool = Utils.copyByte(byteSrc, stylePool, chunkSize - (stylePool));

        return chunk;
    }

    private byte[] getStrListByte(ArrayList<String> strList) {
        byte[] src = new byte[0];
        ArrayList<String> stringContentList = convertStrList(strList);
        Utils.writeDealInfo("stringContentList 的 length : " + stringContentList.size());
        // 由于中文字符 导致了这个问题
//        Utils.writeDealInfo(" stringContentList.get(138).getBytes()的length  : " + stringContentList.get(138));
        for (int i = 0; i < stringContentList.size(); i++) {   // i = 138 时出现了错误
            byte[] tempAry = new byte[0];
            short len = (short) (stringContentList.get(i).length() / 2);
            byte[] lenAry = Utils.shortToByte(len);
            tempAry = Utils.addByte(tempAry, lenAry);
            tempAry = Utils.addByte(tempAry, stringContentList.get(i).getBytes());
            tempAry = Utils.addByte(tempAry, new byte[]{0, 0});
            src = Utils.addByte(src, tempAry);
//            Utils.writeDealInfo("第"+ i +"次 src 的 length : " + src.length);
        }
        return src;
    }

    private ArrayList<String> convertStrList(ArrayList<String> stringContentList) {
        ArrayList<String> destList = new ArrayList<String>(stringContentList.size());
        for (String str : stringContentList) {
            Utils.writeDealInfo("ConverStrList 前 : " + str);
            byte[] temp =  str.getBytes();
            Utils.writeDealInfo("ConverStrList 中 getBytes 的长度 : " + temp.length + " temp的内容 ： " + new String(temp));
            byte[] src = new byte[temp.length * 2];
            Utils.writeDealInfo("src  的长度 : " + src.length);
            for (int i = 0; i < temp.length; i++) {
                src[i * 2] = temp[i];
                src[i * 2 + 1] = 0;
//                Utils.writeDealInfo(i + "： temp[i] = " + temp[i]);
//                Utils.writeDealInfo(i + "： src[i * 2] = " + src[i * 2]);
//                Utils.writeDealInfo(i + "： src[i * 2 + 1] = " + src[i * 2 + 1]);
            }

            String s =  new String(src);
            Utils.writeDealInfo("ConverStrList : " + s);
            destList.add(s);
        }
        return destList;
    }

}
