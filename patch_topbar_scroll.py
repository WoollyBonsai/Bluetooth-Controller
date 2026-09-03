with open('app/src/main/res/layout/activity_main.xml', 'r') as f:
    content = f.read()

content = content.replace(
"""    <LinearLayout
        android:id="@+id/topBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="8dp"
        app:layout_constraintTop_toTopOf="parent">""",
"""    <HorizontalScrollView
        android:id="@+id/topBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:scrollbars="none"
        app:layout_constraintTop_toTopOf="parent">
        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="8dp">"""
)

content = content.replace(
"""            android:entries="@array/mode_array" />
    </LinearLayout>""",
"""            android:entries="@array/mode_array" />
        </LinearLayout>
    </HorizontalScrollView>"""
)

with open('app/src/main/res/layout/activity_main.xml', 'w') as f:
    f.write(content)
