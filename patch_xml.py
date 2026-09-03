with open('app/src/main/res/layout/activity_main.xml', 'r') as f:
    content = f.read()

content = content.replace(
    '    <FrameLayout\n        android:id="@+id/layoutGamepad"',
    '    <FrameLayout\n        android:id="@+id/layoutCustom"\n        android:layout_width="match_parent"\n        android:layout_height="0dp"\n        android:layout_weight="1"\n        android:visibility="gone" />\n\n    <FrameLayout\n        android:id="@+id/layoutGamepad"'
)

with open('app/src/main/res/layout/activity_main.xml', 'w') as f:
    f.write(content)
