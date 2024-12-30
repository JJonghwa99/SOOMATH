import {
  ClassicEditor,
  AccessibilityHelp,
  Alignment,
  Autoformat,
  AutoImage,
  AutoLink,
  Autosave,
  BlockQuote,
  Bold,
  Code,
  CodeBlock,
  Essentials,
  FindAndReplace,
  FontBackgroundColor,
  FontColor,
  FontFamily,
  FontSize,
  GeneralHtmlSupport,
  Heading,
  Highlight,
  HorizontalLine,
  ImageBlock,
  ImageCaption,
  ImageInline,
  ImageInsert,
  ImageInsertViaUrl,
  ImageResize,
  ImageStyle,
  ImageTextAlternative,
  ImageToolbar,
  ImageUpload,
  Indent,
  IndentBlock,
  Italic,
  Link,
  LinkImage,
  List,
  ListProperties,
  MediaEmbed,
  Paragraph,
  PasteFromOffice,
  RemoveFormat,
  SelectAll,
  SimpleUploadAdapter,
  SpecialCharacters,
  SpecialCharactersArrows,
  SpecialCharactersCurrency,
  SpecialCharactersEssentials,
  SpecialCharactersLatin,
  SpecialCharactersMathematical,
  SpecialCharactersText,
  Strikethrough,
  Style,
  Subscript,
  Superscript,
  Table,
  TableCaption,
  TableCellProperties,
  TableColumnResize,
  TableProperties,
  TableToolbar,
  TextTransformation,
  TodoList,
  Underline,
  Undo,
  FileRepository
} from 'ckeditor5';

import translations from 'ckeditor5/translations/ko.js';

// 커스텀 이미지 업로드 어댑터 설정
class MyUploadAdapter {
  constructor(loader) {
    this.loader = loader;
  }

  upload() {
    return this.loader.file
      .then(file => new Promise((resolve, reject) => {
        const data = new FormData();
        data.append('upload', file);

        fetch('/exam/uploadImage', { // 이미지 업로드 서버 URL
          method: 'POST',
          body: data
        })
        .then(response => response.json())
        .then(result => {
          resolve({
            default: result.url // 서버에서 반환된 이미지 URL
          });
        })
        .catch(reject);
      }));
  }

  abort() {
    // 업로드 중단 시 호출됩니다.
  }
}

// 커스텀 이미지 업로드 어댑터 플러그인
function MyCustomUploadAdapterPlugin(editor) {
  editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
    return new MyUploadAdapter(loader);
  };
}

const editorConfig = {
  toolbar: {
    items: [
      'undo', 'redo', '|', 'findAndReplace', '|', 'heading', 'style', '|',
      'fontSize', 'fontFamily', 'fontColor', 'fontBackgroundColor', '|',
      'bold', 'italic', 'underline', 'strikethrough', 'subscript', 'superscript', 'code',
      'removeFormat', '|', 'specialCharacters', 'horizontalLine', 'link',
      'insertImage', 'insertImageViaUrl', 'mediaEmbed', 'insertTable', 'highlight',
      'blockQuote', 'codeBlock', '|', 'alignment', '|', 'bulletedList',
      'numberedList', 'todoList', 'outdent', 'indent'
    ],
    shouldNotGroupWhenFull: true
  },
  plugins: [
    AccessibilityHelp, Alignment, Autoformat, AutoImage, AutoLink, Autosave,
    BlockQuote, Bold, Code, CodeBlock, Essentials, FindAndReplace, FontBackgroundColor,
    FontColor, FontFamily, FontSize, GeneralHtmlSupport, Heading, Highlight,
    HorizontalLine, ImageBlock, ImageCaption, ImageInline, ImageInsert, ImageInsertViaUrl,
    ImageResize, ImageStyle, ImageTextAlternative, ImageToolbar, ImageUpload, Indent,
    IndentBlock, Italic, Link, LinkImage, List, ListProperties, MediaEmbed, Paragraph,
    PasteFromOffice, RemoveFormat, SelectAll, SimpleUploadAdapter, SpecialCharacters,
    SpecialCharactersArrows, SpecialCharactersCurrency, SpecialCharactersEssentials,
    SpecialCharactersLatin, SpecialCharactersMathematical, SpecialCharactersText,
    Strikethrough, Style, Subscript, Superscript, Table, TableCaption, TableCellProperties,
    TableColumnResize, TableProperties, TableToolbar, TextTransformation, TodoList,
    Underline, Undo, FileRepository // FileRepository 추가
  ],
  fontFamily: {
    supportAllValues: true
  },
  fontSize: {
    options: [10,11,12,13,14,15, 'default',17, 18,19, 20, 22,23,25,27,30,32,34],
    supportAllValues: true
  },
  heading: {
    options: [
      { model: 'heading1', view: 'h1', title: 'Heading 1', class: 'ck-heading_heading1' },
      { model: 'paragraph', title: 'Paragraph', class: 'ck-heading_paragraph' },
      { model: 'heading2', view: 'h2', title: 'Heading 2', class: 'ck-heading_heading2' },
      { model: 'heading3', view: 'h3', title: 'Heading 3', class: 'ck-heading_heading3' },
      { model: 'heading4', view: 'h4', title: 'Heading 4', class: 'ck-heading_heading4' },
      { model: 'heading5', view: 'h5', title: 'Heading 5', class: 'ck-heading_heading5' },
      { model: 'heading6', view: 'h6', title: 'Heading 6', class: 'ck-heading_heading6' }
    ],
    default: 'heading3'
  },
  htmlSupport: {
    allow: [
      { name: /^.*$/, styles: true, attributes: true, classes: true }
    ]
  },
  image: {
    toolbar: [
      'toggleImageCaption', 'imageTextAlternative', '|', 'imageStyle:inline',
      'imageStyle:wrapText', 'imageStyle:breakText', '|', 'resizeImage'
    ]
  },
  placeholder: '여기에 입력하세요.',
  language: 'ko',
  link: {
    addTargetToExternalLinks: true,
    defaultProtocol: 'https://',
    decorators: {
      toggleDownloadable: {
        mode: 'manual',
        label: 'Downloadable',
        attributes: { download: 'file' }
      }
    }
  },
  list: {
    properties: {
      styles: true,
      startIndex: true,
      reversed: true
    }
  },
  style: {
    definitions: [
      { name: 'Article category', element: 'h3', classes: ['category'] },
      { name: 'Title', element: 'h2', classes: ['document-title'] },
      { name: 'Subtitle', element: 'h3', classes: ['document-subtitle'] },
      { name: 'Info box', element: 'p', classes: ['info-box'] },
      { name: 'Side quote', element: 'blockquote', classes: ['side-quote'] },
      { name: 'Marker', element: 'span', classes: ['marker'] },
      { name: 'Spoiler', element: 'span', classes: ['spoiler'] },
      { name: 'Code (dark)', element: 'pre', classes: ['fancy-code', 'fancy-code-dark'] },
      { name: 'Code (bright)', element: 'pre', classes: ['fancy-code', 'fancy-code-bright'] }
    ]
  },
  table: {
    contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells', 'tableProperties', 'tableCellProperties']
  },
  mediaEmbed: {
    previewsInData: true
  },
  ckfinder: {
    uploadUrl: '/exam/uploadImage'
  },
  translations: [translations],
  extraPlugins: [MyCustomUploadAdapterPlugin] // 사용자 정의 업로드 어댑터 플러그인 추가
};

// CKEditor 생성
ClassicEditor
  .create(document.querySelector('#editor'), editorConfig)
  .catch(error => {
    console.error(error);
  });
