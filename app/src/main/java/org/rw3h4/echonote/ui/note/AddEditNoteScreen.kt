package org.rw3h4.echonote.ui.note

import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doOnTextChanged
import coil3.compose.AsyncImage
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import org.rw3h4.echonote.R
import org.rw3h4.echonote.data.local.model.Note
import org.rw3h4.echonote.ui.theme.DarkBlue
import org.rw3h4.echonote.ui.theme.LightBlue
import org.rw3h4.echonote.ui.theme.LightPurple
import org.rw3h4.echonote.ui.theme.OffWhite
import org.rw3h4.echonote.viewmodel.AddEditNoteViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: AddEditNoteViewModel,
    existingNote: Note?,
    onSave: (title: String, content: String, category: String) -> Unit,
    onNavigateUp: () -> Unit,
    onLaunchGallery: () -> Unit,
    onLaunchCamera: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    val categories by viewModel.allCategories.observeAsState(listOf())
    var contentParts by remember { mutableStateOf<List<EditContentPart>>(emptyList()) }
    var focusedPartId by remember { mutableStateOf<UUID?>(null) }
    val focusRequesters = remember { mutableStateMapOf<UUID, FocusRequester>() }

    LaunchedEffect(focusedPartId) {
        if (focusedPartId != null) {
            focusRequesters[focusedPartId]?.requestFocus()
        }
    }

    fun handleDeleteAction(partId: UUID) {
        val indexToDelete = contentParts.indexOfFirst { it.id == partId }
        if (indexToDelete == -1) return
        val newParts = contentParts.toMutableList()
        val partToDelete = newParts[indexToDelete]
        val previousPart = newParts.getOrNull(indexToDelete - 1)
        val nextPart = newParts.getOrNull(indexToDelete + 1)
        when {
            partToDelete is EditContentPart.Image && previousPart is EditContentPart.Text && nextPart is EditContentPart.Text -> {
                val mergedText = previousPart.value.text + nextPart.value.text
                val cursorPosition = previousPart.value.text.length
                val mergedTextFieldValue = TextFieldValue(mergedText, selection = TextRange(cursorPosition))
                newParts[indexToDelete - 1] = previousPart.copy(value = mergedTextFieldValue)
                newParts.removeAt(indexToDelete + 1)
                newParts.removeAt(indexToDelete)
                focusedPartId = previousPart.id
            }
            else -> {
                newParts.removeAt(indexToDelete)
                val newPreviousPart = newParts.getOrNull(indexToDelete - 1)
                val currentPart = newParts.getOrNull(indexToDelete)
                if (newPreviousPart is EditContentPart.Text && currentPart is EditContentPart.Text) {
                    val mergedText = newPreviousPart.value.text + currentPart.value.text
                    val cursorPosition = newPreviousPart.value.text.length
                    val mergedTextFieldValue = TextFieldValue(mergedText, selection = TextRange(cursorPosition))
                    newParts[indexToDelete - 1] = newPreviousPart.copy(value = mergedTextFieldValue)
                    newParts.removeAt(indexToDelete)
                    focusedPartId = newPreviousPart.id
                } else {
                    focusedPartId = newParts.getOrNull(indexToDelete - 1)?.id
                }
            }
        }
        if (newParts.isEmpty() || newParts.last() is EditContentPart.Image) {
            newParts.add(EditContentPart.Text(value = TextFieldValue("")))
        }
        contentParts = newParts
    }

    LaunchedEffect(existingNote, categories) {
        if (existingNote != null) {
            title = existingNote.title
            val categoryName = categories.find { it.id == existingNote.categoryId }?.name ?: "None"
            category = categoryName
            if (contentParts.isEmpty()) {
                contentParts = parseHtmlToContentPart(existingNote.content)
            }
        } else {
            if (contentParts.isEmpty()) {
                contentParts = listOf(EditContentPart.Text(value = TextFieldValue("")))
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.imageToInsert.collect { uri ->
            val newParts = contentParts.toMutableList()
            val index = contentParts.indexOfFirst { it.id == focusedPartId }.coerceIn(0, newParts.size - 1)
            val focusedPart = newParts.getOrNull(index)
            if (focusedPart is EditContentPart.Text) {
                val cursorPosition = focusedPart.value.selection.start
                val textBefore = focusedPart.value.text.substring(0, cursorPosition)
                val textAfter = focusedPart.value.text.substring(cursorPosition)
                newParts[index] = focusedPart.copy(value = TextFieldValue(textBefore))
                val imagePart = EditContentPart.Image(uri = uri)
                val textAfterPart = EditContentPart.Text(value = TextFieldValue(textAfter, selection = TextRange(0)))
                newParts.add(index + 1, imagePart)
                newParts.add(index + 2, textAfterPart)
                focusedPartId = textAfterPart.id
            } else {
                val insertPos = if (index != -1) index + 1 else newParts.size
                val imagePart = EditContentPart.Image(uri = uri)
                val textAfterPart = EditContentPart.Text(value = TextFieldValue(""))
                newParts.add(insertPos, imagePart)
                newParts.add(insertPos + 1, textAfterPart)
                focusedPartId = textAfterPart.id
            }
            contentParts = newParts
        }
    }

    Scaffold(
        containerColor = OffWhite,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (existingNote == null) "Add Note" else "Edit Note",
                        color = DarkBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = DarkBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = LightPurple.copy(alpha = 0.5f),
                )
            )
        },
        bottomBar = {
            ElevatedActionBar(
                onSaveClick = {
                    val finalHtml = convertCContentPartsToHtml(contentParts)
                    onSave(title, finalHtml, category)
                },
                onAttachImageClick = onLaunchGallery,
                onTakePhotoClick = onLaunchCamera
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(OffWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightPurple.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = {
                        Text(
                            "Note Title",
                            color = DarkBlue.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue.copy(alpha = 0.5f),
                        unfocusedBorderColor = DarkBlue.copy(alpha = 0.3f),
                        focusedTextColor = DarkBlue,
                        unfocusedTextColor = DarkBlue,
                        cursorColor = DarkBlue,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = DarkBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                CategoryAutoComplete(
                    categories = categories.map { it.name },
                    selectedCategory = category,
                    onCategorySelected = { category = it }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                state = rememberLazyListState(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contentParts, key = { it.id }) { part ->
                    when (part) {
                        is EditContentPart.Text -> {
                            BasicTextField(
                                value = part.value,
                                onValueChange = { newValue ->
                                    contentParts = contentParts.map {
                                        if (it.id == part.id) {
                                            (it as EditContentPart.Text).copy(value = newValue)
                                        } else {
                                            it
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequesters.getOrPut(part.id) { FocusRequester() })
                                    .onFocusChanged {
                                        if (it.isFocused) focusedPartId = part.id
                                    }
                                    .onKeyEvent { event ->
                                        if (event.key == Key.Backspace && part.value.selection.start == 0 && part.value.selection.end == 0) {
                                            val index = contentParts.indexOf(part)
                                            val previousPart = contentParts.getOrNull(index - 1)
                                            if (previousPart != null) {
                                                handleDeleteAction(previousPart.id)
                                            }
                                            return@onKeyEvent true
                                        }
                                        false
                                    },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = DarkBlue),
                                decorationBox = { innerTextField ->
                                    Box(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)) {
                                        if (part.value.text.isEmpty()) {
                                            Text("Start typing...", color = DarkBlue.copy(alpha = 0.5f))
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        is EditContentPart.Image -> {
                            var boxSize by remember { mutableStateOf(0f) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(part.sizeFraction)
                                    .onSizeChanged {
                                        boxSize = it.width.toFloat()
                                    }
                            ) {
                                AsyncImage(
                                    model = part.uri,
                                    contentDescription = "Note Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.FillWidth
                                )
                                IconButton(
                                    onClick = {
                                        handleDeleteAction(part.id)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f), CircleShape
                                        )
                                ) {
                                    Icon(Icons.Default.Delete, "Delete Image", tint = Color.White)
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        .pointerInput(Unit) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                val newSizeFraction = (part.sizeFraction +
                                                        (dragAmount.x / boxSize)).coerceIn(0.3f, 1.0f)
                                                contentParts = contentParts.map { p ->
                                                    if (p.id == part.id) {
                                                        (p as EditContentPart.Image).copy(sizeFraction = newSizeFraction)
                                                    } else {
                                                        p
                                                    }
                                                }
                                            }
                                        }
                                ) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_resize),
                                        contentDescription = "Resize Image",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryAutoComplete(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val currentOnCategorySelected by rememberUpdatedState(onCategorySelected)
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            val textInputLayout = TextInputLayout(context, null, R.style.Widget_App_TextInputLayout)
            val autoComplete = MaterialAutoCompleteTextView(context).apply {
                hint = "Select or create category"
                doOnTextChanged { text, _, _, _ ->
                    if (text.toString() != selectedCategory) {
                        currentOnCategorySelected(text.toString())
                    }
                }
                setOnItemClickListener { _, _, position, _ ->
                    val selected = adapter.getItem(position) as String
                    currentOnCategorySelected(selected)
                }
            }
            textInputLayout.addView(autoComplete)
            textInputLayout
        },
        update = { view ->
            val autoComplete = view.editText as? MaterialAutoCompleteTextView
            autoComplete?.let {
                val adapter = ArrayAdapter(it.context, android.R.layout.simple_dropdown_item_1line, categories)
                it.setAdapter(adapter)
                if (it.text.toString() != selectedCategory) {
                    it.setText(selectedCategory, false)
                }
            }
        }
    )
}

@Composable
fun ElevatedActionBar(
    onSaveClick: () -> Unit,
    onAttachImageClick: () -> Unit,
    onTakePhotoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = LightPurple.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onTakePhotoClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.attach_photo),
                            contentDescription = "Take Photo",
                            tint = DarkBlue
                        )
                    }
                    IconButton(
                        onClick = onAttachImageClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.attach_image),
                            contentDescription = "Attach Image",
                            tint = DarkBlue
                        )
                    }
                }
                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue,
                        contentColor = LightBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "Save",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}