from PIL import Image
import svgwrite
import fontforge

SCALER = 24
OFF = 10

chars = (" !\"#$%&'()*+,-./" +
				"0123456789:;<=>?" +
				"@ABCDEFGHIJKLMNO" +
				"PQRSTUVWXYZ[\\]^_" +
				"'abcdefghijklmno" +
				"pqrstuvwxyz{|}~`" +
				"âăîşţàçæèéêëïôœù" +
				"úûüÿáíóñ¡¿äöå")

def save_char(char_id, cell_width, cell_height, char_img):
    dwg = svgwrite.Drawing(f"chars/{chars[char_id]}.svg", size=(f"{cell_width*SCALER}px", f"{(cell_height-(2*OFF))*SCALER}px"), profile='tiny')
    for y in range(cell_height):
        for x in range(cell_width):
            r, g, b, a = char_img.getpixel((x, y))
            if a > 0:
                hex_color = f'#{r:02x}{g:02x}{b:02x}'
                dwg.add(dwg.rect((x*SCALER, (y-OFF)*SCALER), (SCALER, SCALER), fill=hex_color))

    dwg.save()

def slice_image(image_path, cell_width, cell_height):
    """
    Slices a grid image into individual character PNGs.
    """
    img = Image.open(image_path)
    width, height = img.size

    char_id = 0
    for y in range(0, height, cell_height):
        for x in range(0, width, cell_width):
            box = (x, y, x + cell_width, y + cell_height)
            char_img = img.crop(box)
            if (char_id < len(chars)):
                save_char(char_id, cell_width, cell_height, char_img)
            char_id += 1

def fontify():
    slice_image("src/main/resources/fonts/default/font.png", 32, 64)
    font = fontforge.font()
    font.fontname = "Bullet"
    font.fullname = "Bullet Sans"
    font.familyname = "Bullet"
    font.encoding = "UnicodeBMP"
    font.copyright = "Copyright (c) 2025 Matei Budiu, Parth Iyer"
    for char in chars:
        glyph = font.createChar(ord(char))
        try:
            if char == ' ':
                glyph.width = 15 * SCALER
            else:
                glyph.importOutlines(f"chars/{char}.svg")
                xmin, ymin, xmax, ymax = glyph.boundingBox()
                glyph.width = int(xmax - xmin + 5*SCALER/2)
                glyph.left_side_bearing = int(5*SCALER/2)

        except Exception as e:
            print(f"Error processing {char}: {e}")

    font.autoWidth(0)
    font.generate("src/main/resources/fonts/default/Bullet.ttf")

fontify()
